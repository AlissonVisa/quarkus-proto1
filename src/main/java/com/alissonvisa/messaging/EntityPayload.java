package com.alissonvisa.messaging;

import org.bson.types.ObjectId;

public abstract class EntityPayload implements IdentifiedPayload {

    private ObjectId id;

    public EntityPayload(ObjectId id) {
        this.id = id;
    }

    @Override
    public ObjectId getId() {
        return id;
    }
}
