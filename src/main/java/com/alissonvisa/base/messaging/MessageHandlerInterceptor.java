package com.alissonvisa.base.messaging;

import com.alissonvisa.base.persistence.Entity;
import com.alissonvisa.base.persistence.Stateful;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Stateful
@Interceptor
public class MessageHandlerInterceptor {

    @Inject
    private MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.database")
    private String database;

    @AroundInvoke
    public Object findEntity(InvocationContext invocationContext) throws Exception {
        IdentifiedPayload message = (IdentifiedPayload) invocationContext.getParameters()[0];
        // todo get id from message, call repository and clone using response from repository
        ((Entity) invocationContext.getTarget()).restoreFromDatabase(message.getId(), mongoClient, database);
        return invocationContext.proceed();
    }
}
