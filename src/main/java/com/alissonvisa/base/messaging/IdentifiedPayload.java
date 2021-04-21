package com.alissonvisa.base.messaging;

import org.bson.types.ObjectId;

public interface IdentifiedPayload {

    ObjectId getId();

}
