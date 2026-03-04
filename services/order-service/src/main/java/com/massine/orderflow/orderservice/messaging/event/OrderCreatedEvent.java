package com.massine.orderflow.orderservice.messaging.event;

import com.massine.orderflow.orderservice.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        Long orderId,
        String customerId,
        Long productId,
        int quantity,
        OrderStatus status
) {
    public static OrderCreatedEvent of(
            Long orderId,
            String customerId,
            Long productId,
            int quantity,
            OrderStatus status
    ) {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                orderId,
                customerId,
                productId,
                quantity,
                status
        );
    }
}