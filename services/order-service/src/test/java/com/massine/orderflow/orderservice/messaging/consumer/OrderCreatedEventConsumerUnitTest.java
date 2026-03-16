package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreatedEventConsumerUnitTest {

    @Mock
    private IdempotencyStore idempotencyStore;

    @Mock
    private OrderCreatedEventHandler handler;

    @Mock
    private MessagingMetrics messagingMetrics;

    @InjectMocks
    private OrderCreatedEventConsumer consumer;

    @Test
    void shouldProcessEventAndMarkProcessedWhenFirstTime() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        when(idempotencyStore.isProcessed(event.eventId())).thenReturn(false);

        consumer.onMessage(event);

        verify(idempotencyStore).isProcessed(event.eventId());
        verify(handler).handle(event);
        verify(idempotencyStore).markProcessed(event.eventId());
        verify(messagingMetrics).incrementConsumed();
        verify(messagingMetrics, never()).incrementDuplicate();
        verify(messagingMetrics, never()).incrementFailed();
    }

    @Test
    void shouldSkipDuplicateEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        when(idempotencyStore.isProcessed(event.eventId())).thenReturn(true);

        consumer.onMessage(event);

        verify(idempotencyStore).isProcessed(event.eventId());
        verifyNoInteractions(handler);
        verify(idempotencyStore, never()).markProcessed(any());
        verify(messagingMetrics).incrementDuplicate();
        verify(messagingMetrics, never()).incrementConsumed();
        verify(messagingMetrics, never()).incrementFailed();
    }

    @Test
    void shouldThrowAndIncrementFailedForInvalidPayload() {
        assertThatThrownBy(() -> consumer.onMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid OrderCreatedEvent payload");

        verify(messagingMetrics).incrementFailed();
        verifyNoInteractions(idempotencyStore, handler);
    }

    @Test
    void shouldThrowAndIncrementFailedWhenEventIdIsNull() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                null,
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        assertThatThrownBy(() -> consumer.onMessage(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid OrderCreatedEvent payload");

        verify(messagingMetrics).incrementFailed();
        verifyNoInteractions(handler);
        verify(idempotencyStore, never()).isProcessed(any());
        verify(idempotencyStore, never()).markProcessed(any());
    }

    @Test
    void shouldThrowAndIncrementFailedWhenHandlerFails() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        when(idempotencyStore.isProcessed(event.eventId())).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(handler).handle(event);

        assertThatThrownBy(() -> consumer.onMessage(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");

        verify(idempotencyStore).isProcessed(event.eventId());
        verify(handler).handle(event);
        verify(idempotencyStore, never()).markProcessed(any());
        verify(messagingMetrics).incrementFailed();
        verify(messagingMetrics, never()).incrementConsumed();
        verify(messagingMetrics, never()).incrementDuplicate();
    }
}