package com.massine.orderflow.orderservice.messaging.consumer;

import java.util.UUID;

public interface IdempotencyStore {

    boolean markProcessed(UUID eventId);

}