package com.alissonvisa.messaging;

import org.bson.types.ObjectId;

public class PersonNamePayload extends EntityPayload {

    private String name;

    public PersonNamePayload(ObjectId id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
