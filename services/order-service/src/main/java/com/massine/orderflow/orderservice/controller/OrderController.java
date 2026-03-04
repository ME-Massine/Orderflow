package com.massine.orderflow.orderservice.controller;

import com.massine.orderflow.orderservice.dto.CreateOrderRequest;
import com.massine.orderflow.orderservice.dto.OrderResponse;
import com.massine.orderflow.orderservice.dto.common.ApiError;
import com.massine.orderflow.orderservice.dto.common.OrderPageResponse;
import com.massine.orderflow.orderservice.dto.common.ValidationError;
import com.massine.orderflow.orderservice.entity.OrderStatus;
import com.massine.orderflow.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @Operation(
            summary = "Create an order",
            description = "Creates a new order and returns the created order representation."
    )
    @ApiResponse(responseCode = "201", description = "Order created",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ValidationError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
        return service.create(req);
    }

    @Operation(
            summary = "Get an order by id",
            description = "Returns the order representation for the given id."
    )
    @ApiResponse(responseCode = "200", description = "Order found",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public OrderResponse get(
            @Parameter(description = "Order id", example = "10")
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(
            summary = "List orders (paginated)",
            description = "Returns a stable pagination envelope for orders."
    )
    @ApiResponse(responseCode = "200", description = "Page of orders",
            content = @Content(schema = @Schema(implementation = OrderPageResponse.class)))
    public OrderPageResponse listOrders(
            @Parameter(description = "Spring pageable query params: page, size, sort")
            Pageable pageable
    ) {
        Page<OrderResponse> page = service.listOrders(pageable);
        return OrderPageResponse.from(page);
    }

    @Operation(
            summary = "Update order status",
            description = "Updates the status of an order using a request parameter."
    )
    @ApiResponse(responseCode = "200", description = "Status updated",
            content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    @ApiResponse(responseCode = "400", description = "Missing/invalid status parameter",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Order not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @Parameter(description = "Order id", example = "7")
            @PathVariable Long id,
            @Parameter(description = "New status", example = "CONFIRMED")
            @RequestParam OrderStatus status
    ) {
        return service.updateStatus(id, status);
    }
}