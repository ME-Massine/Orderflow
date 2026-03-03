package com.massine.orderflow.orderservice.service;

import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.exception.NotFoundException;
import com.massine.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * v0.6.0: preferred paging API for controllers.
     * Keeps controller clean and allows Spring to bind page/size/sort automatically.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    /**
     * Backward-compatible helper (can be removed later if unused).
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return listOrders(pageable);
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