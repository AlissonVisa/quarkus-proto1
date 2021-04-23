package com.alissonvisa.base.persistence.lock;

import io.netty.util.internal.StringUtil;
import io.quarkus.redis.client.RedisClient;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
@JBossLog
public class RedisLockerRepository {

    private static final String LOCK_ID_FIELD = "lockId";
    private static final String OK_RESPONSE = "OK";
    private static final String MILLISECONDS = "PX"; // arg milliseconds
    private static final String SECONDS = "EX"; // arg milliseconds

    @ConfigProperty(name = "application.entity-locker.entity-lock-timeout", defaultValue = "4500")
    private Long entityLockTimeout;

    @Inject
    private RedisClient redisClient;

    Boolean setLockIfNotExists(String key, UUID lockId) {
        Boolean responseSet = redisClient.setnx(key, lockId.toString()).toBoolean();
        Boolean responseExpire = false;
        if(responseSet) {
            responseExpire = setExpirationMillis(key, lockId.toString(), entityLockTimeout.toString());
        }
//        Boolean response = redisClient.hsetnx(key, LOCK_ID, value.toString()).toBoolean();
        return responseSet && responseExpire;
    }

//    Boolean forceLock(String key, UUID lockId) {
//        LinkedList<String> args = new LinkedList<>();
//        args.addLast(key);
//        args.addLast(lockId.toString());
//        args.addLast(MILLISECONDS);
//        args.addLast(entityLockTimeout.toString());
//        Boolean response = redisClient.set(args).toString().equals(OK_RESPONSE);
//        System.out.println("reponse force set " + response);
//
////        response = response && setExpirationMillis(key, lockId.toString(), entityLockTimeout);
////        Boolean response = redisClient.hsetnx(key, LOCK_ID, value.toString()).toBoolean();
//        return response;
//    }

    UUID getLockId(String key) {
        String response = redisClient.get(key).toString();
        return StringUtil.isNullOrEmpty(response) ? null : UUID.fromString(response);
    }

    Boolean setExpirationSeconds(String key, String value, String seconds) {
        String response = redisClient.psetex(key, seconds, value).toString();
        log.info("setExpirationSeconds response " + response + " entity " + key);

        return response == OK_RESPONSE;
    }

    Boolean setExpirationMillis(String key, String value, String millis) {
        String response = redisClient.psetex(key, millis, value).toString();
        log.info("setExpirationMillis response " + response + " entity " + key);

        return response.equals(OK_RESPONSE);
    }

    public Boolean removeLock(String entityKey) {
        Short response = redisClient.del(Collections.singletonList(entityKey)).toShort();
        log.info("removeLock response " + response + " entity " + entityKey);
        return redisClient.del(Collections.singletonList(entityKey)).toShort() > 0;
    }
}
