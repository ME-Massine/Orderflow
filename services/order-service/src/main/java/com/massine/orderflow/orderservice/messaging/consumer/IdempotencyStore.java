package com.massine.orderflow.orderservice.messaging.consumer;

import java.util.UUID;

public interface IdempotencyStore {
    boolean isProcessed(UUID eventId);
    void markProcessed(UUID eventId);
}