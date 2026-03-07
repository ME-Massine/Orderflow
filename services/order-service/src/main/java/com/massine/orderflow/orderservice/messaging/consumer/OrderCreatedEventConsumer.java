package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final IdempotencyStore idempotencyStore;
    private final OrderCreatedEventHandler handler;
    private final MessagingMetrics messagingMetrics;

    public OrderCreatedEventConsumer(
            IdempotencyStore idempotencyStore,
            OrderCreatedEventHandler handler,
            MessagingMetrics messagingMetrics
    ) {
        this.idempotencyStore = idempotencyStore;
        this.handler = handler;
        this.messagingMetrics = messagingMetrics;
    }

    @RabbitListener(queues = "${orderflow.rabbitmq.order-created.queue}")
    public void onMessage(OrderCreatedEvent event) {
        if (event == null || event.eventId() == null) {
            messagingMetrics.incrementFailed();
            throw new IllegalArgumentException("Invalid OrderCreatedEvent payload");
        }

        boolean firstTime = idempotencyStore.markProcessed(event.eventId());

        if (!firstTime) {
            messagingMetrics.incrementDuplicate();
            log.info(
                    "Skipping duplicate OrderCreatedEvent eventId={} orderId={}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        try {
            handler.handle(event);
            messagingMetrics.incrementConsumed();
        } catch (RuntimeException ex) {
            messagingMetrics.incrementFailed();
            throw ex;
        }
    }
}