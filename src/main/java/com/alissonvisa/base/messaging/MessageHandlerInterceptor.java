package com.alissonvisa.base.messaging;

import com.alissonvisa.base.persistence.Entity;
import com.alissonvisa.base.persistence.Stateful;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Stateful
@Interceptor
public class MessageHandlerInterceptor {

    @AroundInvoke
    public Object findEntity(InvocationContext invocationContext) throws Exception {
        IdentifiedPayload message = (IdentifiedPayload) invocationContext.getParameters()[0];
        // todo get id from message, call repository and clone using response from repository
        ((Entity) invocationContext.getTarget()).restoreFromDatabase(message.getId());
        return invocationContext.proceed();
    }
}
