package com.massine.orderflow.orderservice.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.messaging.consumer.MessagingMetrics;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEvent;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventRepository;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventStatus;
import com.massine.orderflow.orderservice.messaging.outbox.OutboxEventWriter;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventWriterTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessagingMetrics messagingMetrics;

    @InjectMocks
    private OutboxEventWriter writer;

    @Test
    void shouldPersistOutboxEvent() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .customerId("c1")
                .productId(101L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");

        writer.writeOrderCreated(order);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(repository).save(captor.capture());
        verify(messagingMetrics).incrementOutboxCreated();

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(saved.getAggregateId()).isEqualTo(1L);
        assertThat(saved.getEventType()).isEqualTo("OrderCreatedEvent");
        assertThat(saved.getPayload()).isEqualTo("{json}");
        assertThat(saved.getAggregateType()).isEqualTo("Order");
        assertThat(saved.getEventId()).isNotNull();
    }

    @Test
    void shouldThrowWhenSerializationFails() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .customerId("c1")
                .productId(101L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .build();

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("json fail") {});

        assertThatThrownBy(() -> writer.writeOrderCreated(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to serialize OrderCreatedEvent");

        verify(repository, never()).save(any());
        verifyNoInteractions(messagingMetrics);
    }
}