package com.massine.orderflow.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.exception.GlobalExceptionHandler;
import com.massine.orderflow.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class) // remove if it causes bean issues
class OrderControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService service;

    @Test
    void create_shouldReturn201_andBody() throws Exception {
        // Arrange
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId("cust-1");
        req.setProductId(100L);
        req.setQuantity(2);

        OrderResponse resp = OrderResponse.builder()
                .id(1L)
                .customerId("cust-1")
                .productId(100L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.parse("2026-02-28T00:00:00Z"))
                .build();

        when(service.create(ArgumentMatchers.any(CreateOrderRequest.class))).thenReturn(resp);

        // Act + Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value("cust-1"))
                .andExpect(jsonPath("$.productId").value(100))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").value("2026-02-28T00:00:00Z"));
    }

    @Test
    void create_whenMissingFields_shouldReturn400() throws Exception {
        // Missing required fields should fail validation (@Valid)
        CreateOrderRequest req = new CreateOrderRequest();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenQuantityBelowMin_shouldReturn400() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId("cust-1");
        req.setProductId(100L);
        req.setQuantity(0); // @Min(1)

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_shouldReturn200_andBody() throws Exception {
        OrderResponse resp = OrderResponse.builder()
                .id(10L)
                .customerId("cust-x")
                .productId(200L)
                .quantity(5)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.parse("2026-02-28T01:00:00Z"))
                .build();

        when(service.getById(10L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/orders/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void list_shouldReturn200_andPageShape() throws Exception {
        OrderResponse r1 = OrderResponse.builder()
                .id(1L).customerId("c1").productId(10L).quantity(1)
                .status(OrderStatus.PENDING).createdAt(Instant.parse("2026-02-28T00:00:00Z"))
                .build();

        OrderResponse r2 = OrderResponse.builder()
                .id(2L).customerId("c2").productId(11L).quantity(2)
                .status(OrderStatus.CANCELLED).createdAt(Instant.parse("2026-02-28T00:10:00Z"))
                .build();

        Page<OrderResponse> page = new PageImpl<>(List.of(r1, r2));

        when(service.list(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].status").value("CANCELLED"));
    }

    @Test
    void updateStatus_shouldReturn200_andBody() throws Exception {
        OrderResponse resp = OrderResponse.builder()
                .id(7L)
                .customerId("cust-7")
                .productId(700L)
                .quantity(3)
                .status(OrderStatus.CONFIRMED)
                .createdAt(Instant.parse("2026-02-28T02:00:00Z"))
                .build();

        when(service.updateStatus(7L, OrderStatus.CONFIRMED)).thenReturn(resp);

        // NOTE: status is a request param in your controller
        mockMvc.perform(patch("/api/v1/orders/7/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_whenMissingParam_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/7/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_whenInvalidEnum_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/7/status")
                        .param("status", "SHIPPED")) // not in your enum
                .andExpect(status().isBadRequest());
    }
}