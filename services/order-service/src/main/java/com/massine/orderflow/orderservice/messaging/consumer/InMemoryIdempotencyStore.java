package com.massine.orderflow.orderservice.messaging.consumer;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Set<UUID> seen = ConcurrentHashMap.newKeySet();

    @Override
    public boolean markProcessed(UUID eventId) {
        return seen.add(eventId);
    }
}