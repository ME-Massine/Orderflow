package com.massine.orderflow.orderservice.service;

import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.exception.NotFoundException;
import com.massine.orderflow.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceUnitTest {

    private OrderRepository repo;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(OrderRepository.class);
        service = new OrderService(repo);
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
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
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repo).save(captor.capture());

        Order toSave = captor.getValue();
        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getCustomerId()).isEqualTo("cust-1");
        assertThat(toSave.getProductId()).isEqualTo(10L);
        assertThat(toSave.getQuantity()).isEqualTo(2);

        // Service does not set status/createdAt explicitly; entity does it on persist
        assertThat(toSave.getStatus()).isNull();
        assertThat(toSave.getCreatedAt()).isNull();
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
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getById_whenMissing_shouldThrowNotFound() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(404L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found: 404");

        verify(repo).findById(404L);
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

        // verify pageable exactly
        verify(repo).findAll(pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(2);

        // verify mapping
        assertThat(respPage.getTotalElements()).isEqualTo(2);
        assertThat(respPage.getContent()).hasSize(2);
        assertThat(respPage.getContent().get(0).getId()).isEqualTo(2L);
        assertThat(respPage.getContent().get(1).getStatus()).isEqualTo(OrderStatus.CANCELLED);

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

        // Response mapped
        assertThat(resp.getId()).isEqualTo(7L);
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // Entity mutated (dirty checking style)
        assertThat(existing.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(repo).findById(7L);
        verify(repo, never()).save(any());
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
        verifyNoMoreInteractions(repo);
    }
}