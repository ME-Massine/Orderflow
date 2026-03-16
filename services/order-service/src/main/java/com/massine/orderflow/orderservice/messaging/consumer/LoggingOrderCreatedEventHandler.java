package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOrderCreatedEventHandler implements OrderCreatedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingOrderCreatedEventHandler.class);

    @Override
    public void handle(OrderCreatedEvent event) {

        // DLQ test trigger
        if ("dlq-test".equals(event.customerId())) {
            throw new RuntimeException("Forced DLQ test failure");
        }

        log.info(
                "Consumed OrderCreatedEvent eventId={} orderId={} customerId={}",
                event.eventId(),
                event.orderId(),
                event.customerId()
        );
    }
}