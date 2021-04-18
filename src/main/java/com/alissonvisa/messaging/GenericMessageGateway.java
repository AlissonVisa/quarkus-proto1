package com.alissonvisa.messaging;

import java.util.concurrent.CompletableFuture;

public interface GenericMessageGateway<T> {

    CompletableFuture<Object> send(T message);

}
