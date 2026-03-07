package com.massine.orderflow.orderservice.messaging.consumer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MessagingMetrics {

    private final Counter consumedCounter;
    private final Counter duplicateCounter;
    private final Counter failedCounter;
    private final Counter dlqCounter;

    public MessagingMetrics(MeterRegistry meterRegistry) {
        this.consumedCounter = Counter.builder("orderflow.messaging.events.consumed")
                .description("Number of successfully consumed messaging events")
                .register(meterRegistry);

        this.duplicateCounter = Counter.builder("orderflow.messaging.events.duplicates")
                .description("Number of duplicate messaging events skipped by idempotency guard")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("orderflow.messaging.events.failed")
                .description("Number of messaging events that failed processing before retry/DLQ")
                .register(meterRegistry);

        this.dlqCounter = Counter.builder("orderflow.messaging.events.dlq")
                .description("Number of messages received by dead letter queue consumers")
                .register(meterRegistry);
    }

    public void incrementConsumed() {
        consumedCounter.increment();
    }

    public void incrementDuplicate() {
        duplicateCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }

    public void incrementDlq() {
        dlqCounter.increment();
    }
}