package com.alissonvisa.base.persistence.lock;

import com.alissonvisa.base.persistence.ApplicationEntity;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@JBossLog
@ApplicationScoped
public class EntityLockManager {

    @ConfigProperty(name = "application.entity-locker.entity-lock-timeout")
    private Long entityLockTimeout = 4500L;

    private final ConcurrentHashMap<String, LockedEntitiesWrapper> lockedEntitiesTable;
    private final ConcurrentHashMap<String, LockMonitor> lockMonitors;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> executionQueue;

    public EntityLockManager() {
        this.lockMonitors = new ConcurrentHashMap<>();
        this.lockedEntitiesTable = new ConcurrentHashMap<>();
        this.executionQueue = new ConcurrentHashMap<>();
    }

    public <T extends ApplicationEntity> boolean  entityLock(T entity) {
        if(entity.getId() == null) return true;
        String className = entity.completeClassName();
        String entityIdHexString = entity.getId().toHexString();
        String entityKey = className + "_" + entityIdHexString;
        LockMonitor lockMonitor = lockMonitors.get(entityKey);
        if(lockMonitor == null) {
            lockMonitors.put(entityKey, new LockMonitor(className, entityIdHexString));
            executionQueue.put(entityKey, new ConcurrentLinkedQueue<>());
        }
        synchronized (lockMonitors.get(entityKey)) {
//            log.info("monitoring sync block " + entityKey);
            String threadName = Thread.currentThread().getName();
            final ConcurrentLinkedQueue threadQueue = executionQueue.get(entityKey);
            if (lateOnQueue(threadName, threadQueue)) {
                log.info("late on queue " + threadName + " entity " + entityIdHexString);
                return false;
            }
            if(lockedEntitiesTable.containsKey(className)) {
                if(lockedEntitiesTable.get(className).getLockedEntities().containsKey(entityIdHexString)) {
                    if(lockedEntitiesTable.get(className).getLockedEntities().get(entityIdHexString).timedOut()) {
                        entity.lockId(UUID.randomUUID());
                        lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
                        threadQueue.remove();
                        log.info("entity locked by another thread due to lock timeout " + entityIdHexString + " " + entity.lockId().toString());
//                        log.info("exiting sync block " + entityKey);
                        return true;
                    }
//                    log.info("exiting sync block " + entityKey);
                    return false;
                } else {
                    entity.lockId(UUID.randomUUID());
                    lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
                    log.info("entity locked " + entityIdHexString + " " + entity.lockId().toString());
                }
            } else {
                lockedEntitiesTable.put(className, new LockedEntitiesWrapper(entity, entityLockTimeout));
                log.info("entity locked - locker started " + entityIdHexString + " " + entity.lockId().toString());
            }
            threadQueue.remove();
//            log.info("exiting sync block " + entityKey);
        }
        return true;
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
        String entityIdHexString = entity.getId().toHexString();
        LockedEntity managerLock = lockedEntitiesTable.get(entity.completeClassName()).getLockedEntities().get(entityIdHexString);
        if(managerLock.getLockId().equals(entity.lockId())) {
            lockedEntitiesTable.get(entity.completeClassName()).getLockedEntities().remove(entityIdHexString);
            log.info("entity unlocked " + entityIdHexString + " " + entity.lockId().toString());
        } else {
            log.warn("entity skip unlock because lock ids does not match entity lock [1] " + entity.lockId() + " manager lock [2] " + managerLock.getLockId());
        }
    }
}
