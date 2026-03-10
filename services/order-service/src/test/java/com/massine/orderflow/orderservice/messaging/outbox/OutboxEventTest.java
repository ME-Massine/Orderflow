package com.massine.orderflow.orderservice.messaging.outbox;

import com.massine.orderflow.orderservice.messaging.outbox.OutboxEvent;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import java.util.UUID;

class OutboxEventTest {

    @Test
    void markPublished_shouldUpdateFields() {
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(1L)
                .eventType("OrderCreatedEvent")
                .payload("{}")
                .status(OutboxEventStatus.PENDING)
                .build();

        event.markPublished();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getFailureReason()).isNull();
    }

    @Test
    void markFailed_shouldSetFailureState() {
        OutboxEvent event = OutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(1L)
                .eventType("OrderCreatedEvent")
                .payload("{}")
                .status(OutboxEventStatus.PENDING)
                .build();

        event.markFailed("boom");

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getFailureReason()).contains("boom");
        assertThat(event.getAttemptCount()).isEqualTo(1);
    }
}
