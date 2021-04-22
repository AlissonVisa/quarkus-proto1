package com.alissonvisa.base.persistence.lock;

import com.alissonvisa.util.TimeWatch;
import lombok.extern.jbosslog.JBossLog;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@JBossLog
public class LockedEntity {

    private TimeWatch timeWatch;
    private String entityId;
    private Long timeoutMillis;
    private UUID lockId;

    public LockedEntity(String entityId) {
        this.entityId = entityId;
        this.timeWatch = TimeWatch.start();
    }

    public LockedEntity(String entityId, Long timeoutMillis, UUID lockId) {
        this.entityId = entityId;
        this.lockId = lockId;
        this.timeoutMillis = timeoutMillis;
        this.timeWatch = TimeWatch.start();
    }

    @Override
    public boolean equals(Object o) {
//        if (timedOut()) {
//            log.warn("Entity lock timeout id " + this.entityId);
//            return false;
//        }
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockedEntity that = (LockedEntity) o;
        return Objects.equals(entityId, that.entityId);
    }

    @Override
    public int hashCode() {
//        if(timedOut()) {
//            log.warn("Entity lock timeout id " + this.entityId);
//            return -1;
//        }
        return Objects.hash(entityId);
    }

    public boolean timedOut() {
        if(this.timeoutMillis == null) {
            return false;
        }
        return this.timeWatch.time(TimeUnit.MILLISECONDS) > this.timeoutMillis;
    }
}
