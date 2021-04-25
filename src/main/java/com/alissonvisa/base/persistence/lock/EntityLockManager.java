package com.alissonvisa.base.persistence.lock;

import com.alissonvisa.base.persistence.ApplicationEntity;
import com.alissonvisa.util.TimeWatch;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@JBossLog
@ApplicationScoped
public class EntityLockManager {

    @ConfigProperty(name = "application.entity-locker.entity-lock-timeout")
    Long entityLockTimeout = 4500L;

    @Inject
    RedisLockerRepository lockerRepository;

    private static final long QUEUE_IDLE_TIMEOUT = 1000L;

    private final ConcurrentHashMap<String, LockedEntitiesWrapper> lockedEntitiesTable;
    private final ConcurrentHashMap<String, LockMonitor> lockMonitors;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> executionQueue;
    private final ConcurrentHashMap<String, TimeWatch> executionQueueTimer;

    public EntityLockManager() {
        this.lockMonitors = new ConcurrentHashMap<>();
        this.lockedEntitiesTable = new ConcurrentHashMap<>();
        this.executionQueue = new ConcurrentHashMap<>();
        this.executionQueueTimer = new ConcurrentHashMap<>();
    }

    public <T extends ApplicationEntity> boolean  entityLock(T entity) {
        if(entity.getId() == null) return true;
        String className = entity.completeClassName();
        String entityIdHexString = entity.getId().toHexString();
        String entityKey = getEntityKey(className, entityIdHexString);
        LockMonitor lockMonitor = lockMonitors.get(entityKey);
        if(lockMonitor == null) {
            lockMonitors.put(entityKey, new LockMonitor(className, entityIdHexString));
            executionQueue.put(entityKey, new ConcurrentLinkedQueue<>());
            startExecutionQueueTimer(entityKey);
        }
        synchronized (lockMonitors.get(entityKey)) {
//            log.info("monitoring sync block " + entityKey);
            String threadName = Thread.currentThread().getName();
            final ConcurrentLinkedQueue threadQueue = executionQueue.get(entityKey);
            if (lateOnQueue(threadName, threadQueue)) {
                log.info("late on queue " + threadName + " entity " + entityIdHexString + " queue " + this.executionQueue.get(entityKey));
                if(executionQueueTime(entityKey, TimeUnit.MILLISECONDS) > QUEUE_IDLE_TIMEOUT){
                    log.warn("queue was idle - clearing execution queue " + this.executionQueue.get(entityKey));
                    this.executionQueue.get(entityKey).clear();
                    startExecutionQueueTimer(entityKey);
                }
                return false;
            }
            UUID lockId = UUID.randomUUID();
            if(lockedEntitiesTable.containsKey(className)) {
                if(lockedEntitiesTable.get(className).getLockedEntities().containsKey(entityIdHexString)) {
                    if(lockedEntitiesTable.get(className).getLockedEntities().get(entityIdHexString).timedOut()) {
                        lockedEntitiesTable.get(entity.completeClassName()).getLockedEntities().remove(entityIdHexString);
                        return forceLock(entity, className, entityIdHexString, entityKey, threadQueue, lockId);
                    }
//                    log.info("exiting sync block " + entityKey);
                    return false;
                } else {
                    if (failLockOnCluster(entity, entityIdHexString, entityKey, lockId)) return false;
                    lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
                    log.info("entity locked " + entityIdHexString + " " + entity.lockId().toString());
                }
            } else {
                if (failLockOnCluster(entity, entityIdHexString, entityKey, lockId)) return false;
                lockedEntitiesTable.put(className, new LockedEntitiesWrapper(entity, entityLockTimeout));
                log.info("entity locked - locker started " + entityIdHexString + " " + entity.lockId().toString());
            }
            queueRemoveHead(entityKey);
//            log.info("exiting sync block " + entityKey);
        }
        return true;
    }

    private <T extends ApplicationEntity> boolean forceLock(T entity, String className, String entityIdHexString, String entityKey, ConcurrentLinkedQueue threadQueue, UUID lockId) {
        Boolean response = lockerRepository.setLockIfNotExists(entityKey, lockId);
        if(!response) {
            log.warn("Failed to force lock on cluster " + entityIdHexString + " " + lockId);
            return false;
        }
        if (checkFailOnClusterLock(entityIdHexString, entityKey, lockId)) return false;
        entity.lockId(lockId);
        lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
        queueRemoveHead(entityKey);
        log.info("entity locked by another thread due to lock timeout " + entityIdHexString + " " + entity.lockId().toString());
//                        log.info("exiting sync block " + entityKey);
        return true;
    }

    private String getEntityKey(String className, String entityIdHexString) {
        return className + ":" + entityIdHexString;
    }

    private boolean checkFailOnClusterLock(String entityIdHexString, String entityKey, UUID lockId) {
        UUID lockIdDatabase = lockerRepository.getLockId(entityKey);
        if (lockIdDatabase == null || !lockId.equals(lockIdDatabase)) {
            if (lockIdDatabase != null) {
                log.warn("Failed to lock based on database due to concurrency conflict " + entityIdHexString + " " + lockId);
            }
            return true;
        }
        return false;
    }

    private <T extends ApplicationEntity> boolean failLockOnCluster(T entity, String entityIdHexString, String entityKey, UUID lockId) {
        Boolean lockResponse = lockerRepository.setLockIfNotExists(entityKey, lockId);
        if(!lockResponse) {
            log.warn("Failed to lock on cluster " + entityIdHexString + " " + lockId);
            return true;
        }
        if (checkFailOnClusterLock(entityIdHexString, entityKey, lockId)) {
            return true;
        }
        entity.lockId(lockId);
        return false;
    }

    private boolean lateOnQueue(String threadName, ConcurrentLinkedQueue threadQueue) {
        if(threadQueue.contains(threadName)) {
            if(!threadQueue.peek().equals(threadName)) {
                return true;
            }
        } else if(!threadQueue.isEmpty()) {
            threadQueue.offer(threadName);
            log.info(threadName + " queued " + threadQueue.toString());
            return true;
        } else {
            threadQueue.offer(threadName);
            log.info(threadName + " queued " + threadQueue.toString());
        }
        return false;
    }

    public <T extends ApplicationEntity> void removeLock(T entity) {
        if(entity.getId() == null) return;
        String className = entity.completeClassName();
        String entityIdHexString = entity.getId().toHexString();
        String entityKey = getEntityKey(className, entityIdHexString);
        lockerRepository.removeLock(entityKey);
        LockedEntity managerLock = lockedEntitiesTable.get(className).getLockedEntities().get(entityIdHexString);
        if(managerLock.getLockId().equals(entity.lockId())) {
            lockedEntitiesTable.get(entity.completeClassName()).getLockedEntities().remove(entityIdHexString);
            log.info("entity unlocked " + entityIdHexString + " " + entity.lockId().toString());
        } else {
            log.warn("entity skip unlock because lock ids does not match entity lock [1] " + entity.lockId() + " manager lock [2] " + managerLock.getLockId());
        }
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getExecutionQueue() {
        return executionQueue;
    }

    public boolean queueRemove(String entityKey, String item) {
        boolean remove = this.executionQueue.get(entityKey).remove(item);
        if(this.executionQueue.get(entityKey).isEmpty())
            stopExecutionQueueTimer(entityKey);
        return remove;
    }

    public Object queueRemoveHead(String entityKey) {
        Object remove = this.executionQueue.get(entityKey).poll();
        if(this.executionQueue.get(entityKey).isEmpty())
            stopExecutionQueueTimer(entityKey);
        else
            startExecutionQueueTimer(entityKey);
        return remove;
    }

    private void startExecutionQueueTimer(String entityKey) {
        if (this.executionQueueTimer.get(entityKey) == null) {
            this.executionQueueTimer.put(entityKey, TimeWatch.start());
        } else {
            this.executionQueueTimer.get(entityKey).reset();
        }
    }

    private long executionQueueTime(String entityKey, TimeUnit timeUnit) {
        if (this.executionQueueTimer.get(entityKey) == null) {
            this.executionQueueTimer.put(entityKey, TimeWatch.start());
        }
        return this.executionQueueTimer.get(entityKey).time(timeUnit);
    }

    private void stopExecutionQueueTimer(String entityKey) {
        this.executionQueueTimer.remove(entityKey);
    }
}
