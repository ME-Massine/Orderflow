package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OrderCreatedEventConsumerUnitTest {

    @Test
    void onMessage_firstTime_shouldHandle() {
        IdempotencyStore store = mock(IdempotencyStore.class);
        OrderCreatedEventHandler handler = mock(OrderCreatedEventHandler.class);
        MessagingMetrics metrics = mock(MessagingMetrics.class);

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler, metrics);

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        when(store.markProcessed(eventId)).thenReturn(true);

        consumer.onMessage(event);

        verify(store).markProcessed(eventId);
        verify(handler).handle(event);
        verify(metrics).incrementConsumed();
        verify(metrics, never()).incrementDuplicate();
        verify(metrics, never()).incrementFailed();
    }

    @Test
    void onMessage_duplicate_shouldSkip() {
        IdempotencyStore store = mock(IdempotencyStore.class);
        OrderCreatedEventHandler handler = mock(OrderCreatedEventHandler.class);
        MessagingMetrics metrics = mock(MessagingMetrics.class);

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler, metrics);

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        when(store.markProcessed(eventId)).thenReturn(false);

        consumer.onMessage(event);

        verify(store).markProcessed(eventId);
        verify(metrics).incrementDuplicate();
        verifyNoInteractions(handler);
        verify(metrics, never()).incrementConsumed();
        verify(metrics, never()).incrementFailed();
    }

    @Test
    void onMessage_missingEventId_shouldThrow() {
        IdempotencyStore store = mock(IdempotencyStore.class);
        OrderCreatedEventHandler handler = mock(OrderCreatedEventHandler.class);
        MessagingMetrics metrics = mock(MessagingMetrics.class);

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler, metrics);

        OrderCreatedEvent event = new OrderCreatedEvent(
                null,
                Instant.now(),
                1L,
                "cust-1",
                101L,
                2,
                OrderStatus.PENDING
        );

        try {
            consumer.onMessage(event);
        } catch (IllegalArgumentException ignored) {
        }

        verify(metrics).incrementFailed();
        verifyNoInteractions(store);
        verifyNoInteractions(handler);
        verify(metrics, never()).incrementConsumed();
        verify(metrics, never()).incrementDuplicate();
    }
}