package com.alissonvisa.base.persistence.lock;

import com.alissonvisa.base.persistence.ApplicationEntity;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

@Getter
class LockedEntitiesWrapper {

    private final ConcurrentHashMap<String, LockedEntity> lockedEntities;

    public <T extends ApplicationEntity> LockedEntitiesWrapper(T newEntry, Long timeoutMillis) {
        this.lockedEntities = new ConcurrentHashMap<>();
        this.lockedEntities.put(newEntry.getId().toHexString(), new LockedEntity(newEntry.getId().toHexString(), timeoutMillis, newEntry.lockId()));
    }
}
