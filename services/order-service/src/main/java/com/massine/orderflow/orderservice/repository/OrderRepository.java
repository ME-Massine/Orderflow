package com.massine.orderflow.orderservice.repository;

import com.massine.orderflow.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}