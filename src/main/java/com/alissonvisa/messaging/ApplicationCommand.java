package com.alissonvisa.messaging;

import javax.enterprise.event.Event;
import java.time.Instant;
import java.util.UUID;

public class ApplicationCommand<T extends IdentifiedPayload> implements GenericMessage {

    UUID id;
    Instant timestamp;
    T payload;

    public ApplicationCommand(T payload) {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.payload = payload;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    protected void fire(Event event) {
        event.select(this.getClass(), new CommandTypeQualifier());
        event.fire(this);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", payload=" + payload != null ? payload.toString() : "null" +
                '}';
    }
}
