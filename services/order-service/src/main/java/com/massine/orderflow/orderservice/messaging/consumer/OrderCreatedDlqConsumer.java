package com.massine.orderflow.orderservice.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OrderCreatedDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedDlqConsumer.class);

    private final MessagingMetrics messagingMetrics;

    public OrderCreatedDlqConsumer(MessagingMetrics messagingMetrics) {
        this.messagingMetrics = messagingMetrics;
    }

    @RabbitListener(queues = "${orderflow.rabbitmq.order-created.dlq}")
    public void onDlqMessage(Message message) {
        messagingMetrics.incrementDlq();

        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        Object originalExchange = headers.get("x-original-exchange");
        Object originalRoutingKey = headers.get("x-original-routingKey");
        Object exceptionMessage = headers.get("x-exception-message");
        Object exceptionStacktrace = headers.get("x-exception-stacktrace");

        log.error(
                """
                DLQ message received:
                queue={}
                originalExchange={}
                originalRoutingKey={}
                exceptionMessage={}
                exceptionStacktrace={}
                payload={}
                """,
                message.getMessageProperties().getConsumerQueue(),
                originalExchange,
                originalRoutingKey,
                exceptionMessage,
                exceptionStacktrace,
                payload
        );
    }
}