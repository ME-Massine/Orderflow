package com.massine.orderflow.orderservice.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${orderflow.rabbitmq.exchange}")
    private String eventsExchange;

    @Value("${orderflow.rabbitmq.dlx-exchange}")
    private String dlxExchange;

    @Value("${orderflow.rabbitmq.order-created.queue}")
    private String orderCreatedQueue;

    @Value("${orderflow.rabbitmq.order-created.routing-key}")
    private String orderCreatedRoutingKey;

    @Value("${orderflow.rabbitmq.order-created.dlq}")
    private String orderCreatedDlq;

    @Value("${orderflow.rabbitmq.order-created.dlq-routing-key}")
    private String orderCreatedDlqRoutingKey;

    @Bean
    public TopicExchange orderflowEventsExchange() {
        return new TopicExchange(eventsExchange);
    }

    @Bean
    public DirectExchange orderflowDlxExchange() {
        return new DirectExchange(dlxExchange);
    }

    @Bean
    public Queue orderCreatedQueue() {
        // With republish recoverer, DLQ routing does not need DLX args here.
        return QueueBuilder.durable(orderCreatedQueue).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderflowEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderflowEventsExchange)
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable(orderCreatedDlq).build();
    }

    @Bean
    public Binding orderCreatedDlqBinding(Queue orderCreatedDlq, DirectExchange orderflowDlxExchange) {
        return BindingBuilder.bind(orderCreatedDlq)
                .to(orderflowDlxExchange)
                .with(orderCreatedDlqRoutingKey);
    }
}