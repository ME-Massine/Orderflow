package com.massine.orderflow.orderservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OrderFlow - Order Service API",
                version = "${spring.application.version}",
                description = "Order management API for OrderFlow (create, retrieve, list, status update)."
        )
)
public class OpenApiConfig {
}