package com.alissonvisa.messaging;

import org.bson.types.ObjectId;

public class PersonPayload implements IdentifiedPayload {

    private String name;

    public PersonPayload(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    @Override
    public ObjectId getId() {
        return null;
    }
}
