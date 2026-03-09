package com.massine.orderflow.orderservice.messaging.publisher;

import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMqEventPublisherTest {

    @Test
    void publishOrderCreated_shouldSendToConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

        RabbitMqEventPublisher publisher = new RabbitMqEventPublisher(
                rabbitTemplate,
                "orderflow.order.events",
                "order.created"
        );

        OrderCreatedEvent event = OrderCreatedEvent.of(
                1L, "c1", 101L, 2, OrderStatus.PENDING
        );

        publisher.publishOrderCreated(event);

        verify(rabbitTemplate).convertAndSend("orderflow.order.events", "order.created", event);
    }
}