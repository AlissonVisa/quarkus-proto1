package com.alissonvisa.base.messaging;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class MessageMetaData {

    public MessageMetaData() {
        this.identifier = UUID.randomUUID();
        this.timestamp = Instant.now();
    }

    private final UUID identifier;
    private final Instant timestamp;

}
