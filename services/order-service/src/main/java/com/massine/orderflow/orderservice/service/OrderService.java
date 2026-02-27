package com.massine.orderflow.orderservice.service;

import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.exception.NotFoundException;
import com.massine.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repo;

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        Order order = Order.builder()
                .customerId(req.getCustomerId())
                .productId(req.getProductId())
                .quantity(req.getQuantity())
                .build();

        Order saved = repo.save(order);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        order.setStatus(status);
        return toResponse(order);
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .productId(o.getProductId())
                .quantity(o.getQuantity())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
}