package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;

public interface OrderCreatedEventHandler {
    void handle(OrderCreatedEvent event);
}