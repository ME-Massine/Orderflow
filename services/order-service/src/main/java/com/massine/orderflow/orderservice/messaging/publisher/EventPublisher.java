package com.massine.orderflow.orderservice.messaging.publisher;

import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;

public interface EventPublisher {
    void publishOrderCreated(OrderCreatedEvent event);
}