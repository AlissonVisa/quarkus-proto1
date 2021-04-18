package com.alissonvisa.domain.person;

import com.alissonvisa.messaging.GenericMessage;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@MessageHandler
public class MessageHandlerInterceptor {

    @AroundInvoke
    public Object findEntity(InvocationContext invocationContext) throws Exception {
        GenericMessage message = (GenericMessage) invocationContext.getParameters()[0];
        // todo get id from message, call repository and clone using response from repository
        ((CloneableEntity) invocationContext.getTarget()).clone(message.getPayload());
        return invocationContext.proceed();
    }
}
