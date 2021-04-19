package com.alissonvisa.messaging;

import org.bson.types.ObjectId;

public interface IdentifiedPayload {

    ObjectId getId();

}
