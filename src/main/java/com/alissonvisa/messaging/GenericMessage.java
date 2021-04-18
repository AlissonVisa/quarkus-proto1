package com.alissonvisa.messaging;

import java.time.Instant;
import java.util.UUID;

public interface GenericMessage<T> {

    UUID getId();
    Instant getTimestamp();
    T getPayload();

}
