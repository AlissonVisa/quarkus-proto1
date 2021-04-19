package com.alissonvisa.domain.person;

import com.alissonvisa.messaging.GenericMessage;
import com.alissonvisa.messaging.IdentifiedPayload;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Stateful
public class MessageHandlerInterceptor {

    @AroundInvoke
    public Object findEntity(InvocationContext invocationContext) throws Exception {
        IdentifiedPayload message = (IdentifiedPayload) ((GenericMessage) invocationContext.getParameters()[0]).getPayload();
        // todo get id from message, call repository and clone using response from repository
        ((Entity) invocationContext.getTarget()).restoreFromDatabase(message.getId());
        return invocationContext.proceed();
    }
}
