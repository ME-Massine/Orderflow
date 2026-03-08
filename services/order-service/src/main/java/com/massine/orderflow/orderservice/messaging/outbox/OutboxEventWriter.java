package com.massine.orderflow.orderservice.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.messaging.consumer.MessagingMetrics;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private static final String AGGREGATE_TYPE_ORDER = "Order";
    private static final String EVENT_TYPE_ORDER_CREATED = "OrderCreatedEvent";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final MessagingMetrics messagingMetrics;

    public void writeOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.of(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getStatus()
        );

        String payload = serialize(event);

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(event.eventId())
                .aggregateType(AGGREGATE_TYPE_ORDER)
                .aggregateId(order.getId())
                .eventType(EVENT_TYPE_ORDER_CREATED)
                .payload(payload)
                .status(OutboxEventStatus.PENDING)
                .build();

        outboxEventRepository.save(outboxEvent);
        messagingMetrics.incrementOutboxCreated();
    }

    private String serialize(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OrderCreatedEvent", e);
        }
    }
}