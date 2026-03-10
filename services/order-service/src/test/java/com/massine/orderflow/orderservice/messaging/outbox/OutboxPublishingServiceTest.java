package com.massine.orderflow.orderservice.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.messaging.consumer.MessagingMetrics;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEvent;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventRepository;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventStatus;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxPublishingService;
import com.massine.orderflow.orderservice.messaging.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublishingServiceTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessagingMetrics messagingMetrics;

    @InjectMocks
    private OutboxPublishingService service;

    @Test
    void publish_shouldMarkPublished_whenEventIsValid() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("OrderCreatedEvent")
                .payload("{json}")
                .status(OutboxEventStatus.PENDING)
                .build();

        OrderCreatedEvent event = OrderCreatedEvent.of(
                10L, "c1", 101L, 2, OrderStatus.PENDING
        );

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue("{json}", OrderCreatedEvent.class)).thenReturn(event);

        service.publish(1L);

        verify(eventPublisher).publishOrderCreated(event);
        verify(messagingMetrics).incrementOutboxPublished();
        verify(messagingMetrics, never()).incrementOutboxFailed();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(1);
        assertThat(outboxEvent.getFailureReason()).isNull();
    }

    @Test
    void publish_shouldMarkFailed_whenPublisherThrows() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("OrderCreatedEvent")
                .payload("{json}")
                .status(OutboxEventStatus.PENDING)
                .build();

        OrderCreatedEvent event = OrderCreatedEvent.of(
                10L, "c1", 101L, 2, OrderStatus.PENDING
        );

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue("{json}", OrderCreatedEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("broker down")).when(eventPublisher).publishOrderCreated(event);

        service.publish(1L);

        verify(messagingMetrics).incrementOutboxFailed();
        verify(messagingMetrics, never()).incrementOutboxPublished();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(outboxEvent.getFailureReason()).contains("broker down");
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(1);
        assertThat(outboxEvent.getLastAttemptAt()).isNotNull();
    }

    @Test
    void publish_shouldMarkFailed_whenEventTypeUnsupported() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("UnknownEvent")
                .payload("{json}")
                .status(OutboxEventStatus.PENDING)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));

        service.publish(1L);

        verifyNoInteractions(eventPublisher, objectMapper);
        verify(messagingMetrics).incrementOutboxFailed();
        verify(messagingMetrics, never()).incrementOutboxPublished();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(outboxEvent.getFailureReason()).contains("Unsupported event type");
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void publish_shouldMarkFailed_whenDeserializationFails() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("OrderCreatedEvent")
                .payload("{bad-json}")
                .status(OutboxEventStatus.PENDING)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue("{bad-json}", OrderCreatedEvent.class))
                .thenThrow(new RuntimeException("json parse error"));

        service.publish(1L);

        verifyNoInteractions(eventPublisher);
        verify(messagingMetrics).incrementOutboxFailed();
        verify(messagingMetrics, never()).incrementOutboxPublished();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(outboxEvent.getFailureReason()).contains("json parse error");
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void publish_shouldReturnImmediately_whenStatusIsNotPending() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("OrderCreatedEvent")
                .payload("{json}")
                .status(OutboxEventStatus.PUBLISHED)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));

        service.publish(1L);

        verifyNoInteractions(eventPublisher, objectMapper, messagingMetrics);

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(outboxEvent.getAttemptCount()).isEqualTo(0);
    }

    @Test
    void publish_shouldThrow_whenOutboxEventNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Outbox event not found: 99");

        verifyNoInteractions(eventPublisher, objectMapper, messagingMetrics);
    }

    @Test
    void publish_shouldIncrementRetryMetric_whenRetryingFailedEvent() throws Exception {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .aggregateType("Order")
                .aggregateId(10L)
                .eventType("OrderCreatedEvent")
                .payload("{json}")
                .status(OutboxEventStatus.FAILED)
                .attemptCount(1)
                .build();

        OrderCreatedEvent event = OrderCreatedEvent.of(
                10L, "c1", 101L, 2, OrderStatus.PENDING
        );

        when(repository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue("{json}", OrderCreatedEvent.class)).thenReturn(event);

        service.publish(1L);

        verify(messagingMetrics).incrementOutboxRetried();
        verify(messagingMetrics).incrementOutboxPublished();

        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
    }


}