package com.massine.orderflow.orderservice.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.massine.orderflow.orderservice.messaging.consumer.MessagingMetrics;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import com.massine.orderflow.orderservice.messaging.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublishingService {

    private static final String ORDER_CREATED_EVENT_TYPE = "OrderCreatedEvent";

    private final OutboxEventRepository repository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final MessagingMetrics messagingMetrics;

    @Transactional
    public void publish(Long outboxEventId) {
        OutboxEvent outboxEvent = repository.findById(outboxEventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + outboxEventId));

        if (outboxEvent.getStatus() != OutboxEventStatus.PENDING &&
                outboxEvent.getStatus() != OutboxEventStatus.FAILED) {
            return;
        }

        try {
            if (!ORDER_CREATED_EVENT_TYPE.equals(outboxEvent.getEventType())) {
                throw new IllegalStateException("Unsupported event type: " + outboxEvent.getEventType());
            }

            OrderCreatedEvent event = objectMapper.readValue(
                    outboxEvent.getPayload(),
                    OrderCreatedEvent.class
            );

            if (outboxEvent.getStatus() == OutboxEventStatus.FAILED) {
                messagingMetrics.incrementOutboxRetried();
            }

            eventPublisher.publishOrderCreated(event);

            outboxEvent.markPublished();
            messagingMetrics.incrementOutboxPublished();

            log.info("Published outbox event id={} eventId={} aggregateId={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventId(),
                    outboxEvent.getAggregateId());

        } catch (Exception ex) {
            outboxEvent.markFailed(ex.getMessage());
            messagingMetrics.incrementOutboxFailed();

            log.error("Failed to publish outbox event id={} eventId={} aggregateId={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventId(),
                    outboxEvent.getAggregateId(),
                    ex);
        }
    }
}