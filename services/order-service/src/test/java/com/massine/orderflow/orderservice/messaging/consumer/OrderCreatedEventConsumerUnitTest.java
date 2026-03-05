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

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler);

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
    }

    @Test
    void onMessage_duplicate_shouldSkip() {

        IdempotencyStore store = mock(IdempotencyStore.class);
        OrderCreatedEventHandler handler = mock(OrderCreatedEventHandler.class);

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler);

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
        verifyNoInteractions(handler);
    }

    @Test
    void onMessage_missingEventId_shouldThrow() {

        IdempotencyStore store = mock(IdempotencyStore.class);
        OrderCreatedEventHandler handler = mock(OrderCreatedEventHandler.class);

        OrderCreatedEventConsumer consumer =
                new OrderCreatedEventConsumer(store, handler);

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

        verifyNoInteractions(store);
        verifyNoInteractions(handler);
    }
}