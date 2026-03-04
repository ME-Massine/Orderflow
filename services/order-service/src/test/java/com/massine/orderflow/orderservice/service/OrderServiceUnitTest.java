package com.massine.orderflow.orderservice.service;

import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.exception.NotFoundException;
import com.massine.orderflow.orderservice.messaging.event.OrderCreatedEvent;
import com.massine.orderflow.orderservice.messaging.publisher.EventPublisher;
import com.massine.orderflow.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository repo;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService service;

    @Test
    void create_shouldPersistReturnResponse_andPublishOrderCreatedEvent() {
        // Arrange
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId("cust-1");
        req.setProductId(10L);
        req.setQuantity(2);

        Order saved = Order.builder()
                .id(1L)
                .customerId("cust-1")
                .productId(10L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.parse("2026-02-28T00:00:00Z"))
                .build();

        when(repo.save(any(Order.class))).thenReturn(saved);

        // Act
        OrderResponse resp = service.create(req);

        // Assert response
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getCustomerId()).isEqualTo("cust-1");
        assertThat(resp.getProductId()).isEqualTo(10L);
        assertThat(resp.getQuantity()).isEqualTo(2);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(resp.getCreatedAt()).isEqualTo(Instant.parse("2026-02-28T00:00:00Z"));

        // Assert what was saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repo).save(orderCaptor.capture());

        Order toSave = orderCaptor.getValue();
        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getCustomerId()).isEqualTo("cust-1");
        assertThat(toSave.getProductId()).isEqualTo(10L);
        assertThat(toSave.getQuantity()).isEqualTo(2);

        // Service does not set status/createdAt explicitly; entity does it on persist
        assertThat(toSave.getStatus()).isNull();
        assertThat(toSave.getCreatedAt()).isNull();

        // Assert event published
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishOrderCreated(eventCaptor.capture());

        OrderCreatedEvent evt = eventCaptor.getValue();
        assertThat(evt.eventId()).isNotNull();
        assertThat(evt.occurredAt()).isNotNull();
        assertThat(evt.orderId()).isEqualTo(1L);
        assertThat(evt.customerId()).isEqualTo("cust-1");
        assertThat(evt.productId()).isEqualTo(10L);
        assertThat(evt.quantity()).isEqualTo(2);
        assertThat(evt.status()).isEqualTo(OrderStatus.PENDING);

        verifyNoMoreInteractions(repo, eventPublisher);
    }

    @Test
    void getById_whenFound_shouldReturnResponse() {
        Order order = Order.builder()
                .id(5L)
                .customerId("cust-x")
                .productId(99L)
                .quantity(1)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.parse("2026-02-28T01:00:00Z"))
                .build();

        when(repo.findById(5L)).thenReturn(Optional.of(order));

        OrderResponse resp = service.getById(5L);

        assertThat(resp.getId()).isEqualTo(5L);
        assertThat(resp.getCustomerId()).isEqualTo("cust-x");
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(repo).findById(5L);
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getById_whenMissing_shouldThrowNotFound() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(404L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found: 404");

        verify(repo).findById(404L);
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void list_shouldCallFindAllWithPageRequest_andMapToResponses() {
        Order o1 = Order.builder()
                .id(2L).customerId("c2").productId(20L).quantity(2)
                .status(OrderStatus.PENDING).createdAt(Instant.parse("2026-02-28T00:10:00Z"))
                .build();
        Order o2 = Order.builder()
                .id(1L).customerId("c1").productId(10L).quantity(1)
                .status(OrderStatus.CANCELLED).createdAt(Instant.parse("2026-02-28T00:00:00Z"))
                .build();

        Page<Order> entityPage = new PageImpl<>(List.of(o1, o2), PageRequest.of(0, 2), 2);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repo.findAll(any(Pageable.class))).thenReturn(entityPage);

        Page<OrderResponse> respPage = service.list(0, 2);

        verify(repo).findAll(pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(2);

        assertThat(respPage.getTotalElements()).isEqualTo(2);
        assertThat(respPage.getContent()).hasSize(2);
        assertThat(respPage.getContent().get(0).getId()).isEqualTo(2L);
        assertThat(respPage.getContent().get(1).getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateStatus_whenFound_shouldMutateEntity_andReturnResponse_withoutSaving() {
        Order existing = Order.builder()
                .id(7L)
                .customerId("cust-7")
                .productId(70L)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.parse("2026-02-28T02:00:00Z"))
                .build();

        when(repo.findById(7L)).thenReturn(Optional.of(existing));

        OrderResponse resp = service.updateStatus(7L, OrderStatus.CONFIRMED);

        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(existing.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(repo).findById(7L);
        verify(repo, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updateStatus_whenMissing_shouldThrowNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L, OrderStatus.CANCELLED))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found: 99");

        verify(repo).findById(99L);
        verify(repo, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(repo);
    }
}