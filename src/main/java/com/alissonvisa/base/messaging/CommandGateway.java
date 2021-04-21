package com.alissonvisa.base.messaging;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Named
@Singleton
public class CommandGateway implements GenericMessageGateway<ApplicationCommand> {

    @Inject
    private @CommandType Event<GenericMessage> event;

    @Override
    public CompletableFuture<Object> send(ApplicationCommand message) {
        message.fire(event);
        return null;
    }
}
