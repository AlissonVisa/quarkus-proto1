package com.alissonvisa.base.messaging;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.enterprise.event.Event;
import java.io.Serializable;

@Getter
public abstract class ApplicationCommand implements GenericMessage, IdentifiedPayload, Serializable {

    private MessageMetaData metaData = new MessageMetaData();

    protected void fire(Event event) {
        event.select(this.getClass(), new CommandTypeQualifier());
        event.fire(this);
    }

    @Override
    public ObjectId getId() {
        return null;
    }
}
