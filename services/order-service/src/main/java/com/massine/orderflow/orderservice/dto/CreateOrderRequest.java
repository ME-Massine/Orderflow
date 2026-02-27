package com.massine.orderflow.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}