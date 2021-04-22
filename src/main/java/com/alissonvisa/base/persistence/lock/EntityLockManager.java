package com.alissonvisa.base.persistence.lock;

import com.alissonvisa.base.persistence.ApplicationEntity;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@JBossLog
@ApplicationScoped
public class EntityLockManager {

    @ConfigProperty(name = "application.entity-locker.entity-lock-timeout")
    private Long entityLockTimeout = 4500L;

    private final ConcurrentHashMap<String, LockedEntitiesWrapper> lockedEntitiesTable;
    private final ConcurrentHashMap<String, LockMonitor> lockMonitors;

    public EntityLockManager() {
        this.lockMonitors = new ConcurrentHashMap<>();
        this.lockedEntitiesTable = new ConcurrentHashMap<>();
    }

    public <T extends ApplicationEntity> boolean  entityLock(T entity) {
        if(entity.getId() == null) return true;
        String className = entity.completeClassName();
        String entityIdHexString = entity.getId().toHexString();
        String monitorKey = className + "_" + entityIdHexString;
        LockMonitor lockMonitor = lockMonitors.get(monitorKey);
        if(lockMonitor == null) {
            lockMonitors.put(monitorKey, new LockMonitor(className, entityIdHexString));
        }
        synchronized (lockMonitors.get(monitorKey)) {
            log.info("monitoring sync block " + monitorKey);
            try {
                Thread.sleep(4000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(lockedEntitiesTable.containsKey(className)) {
                if(lockedEntitiesTable.get(className).getLockedEntities().containsKey(entityIdHexString)) {
                    if(lockedEntitiesTable.get(className).getLockedEntities().get(entityIdHexString).timedOut()) {
                        entity.lockId(UUID.randomUUID());
                        lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
                        log.info("entity locked by another thread due to lock timeout " + entityIdHexString + " " + entity.lockId().toString());
                        log.info("exiting sync block " + monitorKey);
                        return true;
                    }
                    log.info("exiting sync block " + monitorKey);
                    return false;
                } else {
                    entity.lockId(UUID.randomUUID());
                    lockedEntitiesTable.get(className).getLockedEntities().put(entityIdHexString, new LockedEntity(entityIdHexString, entityLockTimeout, entity.lockId()));
                    log.info("entity locked " + entityIdHexString + " " + entity.lockId().toString());
                }
            } else {
                lockedEntitiesTable.put(className, new LockedEntitiesWrapper(entity, entityLockTimeout));
                log.info("locker started - entity locked " + entityIdHexString + " " + entity.lockId().toString());
            }
            log.info("exiting sync block " + monitorKey);
        }
        return true;
    }

    public <T extends ApplicationEntity> void removeLock(T entity) {
        String entityIdHexString = entity.getId().toHexString();
        lockedEntitiesTable.get(entity.completeClassName()).getLockedEntities().remove(entityIdHexString);
        log.info("entity unlocked " + entity.lockId().toString());
    }
}
