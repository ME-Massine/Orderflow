package com.massine.orderflow.orderservice.repository;

import com.massine.orderflow.orderservice.entity.Order;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryDataJpaTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void save_shouldPersistAndAssignId_andDefaultsShouldApply() {
        Order o = Order.builder()
                .customerId("cust-1")
                .productId(100L)
                .quantity(2)
                // status null on purpose to test @PrePersist default
                // createdAt null on purpose to test @PrePersist default
                .build();

        Order saved = repository.save(o);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("cust-1");
        assertThat(saved.getProductId()).isEqualTo(100L);
        assertThat(saved.getQuantity()).isEqualTo(2);

        // PrePersist should set defaults
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void save_whenExplicitFieldsProvided_shouldKeepThem() {
        Instant ts = Instant.parse("2026-02-28T03:00:00Z");

        Order o = Order.builder()
                .customerId("cust-x")
                .productId(200L)
                .quantity(5)
                .status(OrderStatus.CONFIRMED)
                .createdAt(ts)
                .build();

        Order saved = repository.save(o);

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(saved.getCreatedAt()).isEqualTo(ts);
    }

    @Test
    void findById_shouldReturnEntity() {
        Order saved = repository.save(Order.builder()
                .customerId("find-me")
                .productId(300L)
                .quantity(1)
                .build());

        Order found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCustomerId()).isEqualTo("find-me");
    }
}