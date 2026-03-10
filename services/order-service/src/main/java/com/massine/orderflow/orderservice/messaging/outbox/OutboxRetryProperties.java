package com.massine.orderflow.orderservice.messaging.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "orderflow.outbox.retry")
public class OutboxRetryProperties {

    private int maxAttempts = 5;
    private long initialDelayMs = 5000;
    private double multiplier = 2.0;
}