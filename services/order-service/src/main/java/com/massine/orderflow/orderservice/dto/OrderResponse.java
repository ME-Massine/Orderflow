package com.massine.orderflow.orderservice.dto;

import com.massine.orderflow.orderservice.entity.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class OrderResponse {
    Long id;
    String customerId;
    Long productId;
    Integer quantity;
    OrderStatus status;
    Instant createdAt;
}