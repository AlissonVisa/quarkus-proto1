package com.alissonvisa.base.messaging;

import com.alissonvisa.base.exception.EntityRestoreTimeoutException;
import com.alissonvisa.base.persistence.ApplicationEntity;
import com.alissonvisa.base.persistence.Stateful;
import com.alissonvisa.base.persistence.lock.EntityLockManager;
import com.alissonvisa.util.TimeWatch;
import com.mongodb.client.MongoClient;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.TimeUnit;

@Stateful
@Interceptor
@JBossLog
public class MessageHandlerInterceptor {

    @Inject
    private MongoClient mongoClient;

    @Inject
    private EntityLockManager lockManager;

    @ConfigProperty(name = "quarkus.mongodb.database")
    private String database;

    @ConfigProperty(name = "application.entity-locker.entity-restore-timeout")
    private Long entityRestoreTimeout = 5000L;

    @ConfigProperty(name = "application.entity-locker.enabled", defaultValue = "true")
    private Boolean entityLockerEnabled;

    @AroundInvoke
    public Object findEntity(InvocationContext invocationContext) throws Exception {
        IdentifiedPayload message = (IdentifiedPayload) invocationContext.getParameters()[0];
        ApplicationEntity entity = ((ApplicationEntity) invocationContext.getTarget());
        entity.setId(message.getId());
        tryLockEntity(entity);
        return invocationContext.proceed();
    }

    private void tryLockEntity(ApplicationEntity entity) throws InterruptedException {
        if(entityLockerEnabled == Boolean.FALSE) return;
        boolean entityLock = false;
        TimeWatch timeWatch = TimeWatch.start();
        while (!entityLock) {
//            log.info("Trying to acquire lock for entity " + entity.getId().toHexString());
            entityLock = lockManager.entityLock(entity);
            if(entityLock) {
                entity.restoreFromDatabase(entity.getId(), mongoClient, database);
            } else {
                Thread.sleep(240L);
                if(timeWatch.time(TimeUnit.MILLISECONDS) > entityRestoreTimeout) {
                    lockManager.getExecutionQueue()
                            .get(getEntityKey(entity.completeClassName(), entity.getId().toHexString()))
                            .remove(Thread.currentThread().getName());
                    throw new EntityRestoreTimeoutException("Entity restore from database timeout. " +
                            "EntityId = " + entity.getId().toHexString() + " " +
                            "elapsed time = " + timeWatch.time(TimeUnit.SECONDS) + " seconds.");
                }
            }
        }
    }

    private String getEntityKey(String className, String entityIdHexString) {
        return className + ":" + entityIdHexString;
    }
}
