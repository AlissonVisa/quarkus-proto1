package com.alissonvisa.base.persistence.lock;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class LockMonitor {

    private String className;

    private String entityId;

    public LockMonitor(String className, String entityId) {
        this.className = className;
        this.entityId = entityId;
    }
}
