package com.massine.orderflow.orderservice.messaging.publisher;

import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String orderCreatedRoutingKey;

    public RabbitMqEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${orderflow.rabbitmq.exchange}") String exchangeName,
            @Value("${orderflow.rabbitmq.order-created.routing-key}") String orderCreatedRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.orderCreatedRoutingKey = orderCreatedRoutingKey;
    }

    @Override
    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, orderCreatedRoutingKey, event);
    }
}