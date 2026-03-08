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

    private final Counter outboxCreatedCounter;
    private final Counter outboxPublishedCounter;
    private final Counter outboxFailedCounter;
    private final Counter outboxRetriedCounter;

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

        this.outboxCreatedCounter = Counter.builder("orderflow.outbox.events.created")
                .description("Number of outbox events created in the database")
                .register(meterRegistry);

        this.outboxPublishedCounter = Counter.builder("orderflow.outbox.events.published")
                .description("Number of outbox events successfully published to RabbitMQ")
                .register(meterRegistry);

        this.outboxFailedCounter = Counter.builder("orderflow.outbox.events.failed")
                .description("Number of outbox events that failed publication")
                .register(meterRegistry);

        this.outboxRetriedCounter = Counter.builder("orderflow.outbox.events.retried")
                .description("Number of outbox retry attempts")
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

    public void incrementOutboxCreated() {
        outboxCreatedCounter.increment();
    }

    public void incrementOutboxPublished() {
        outboxPublishedCounter.increment();
    }

    public void incrementOutboxFailed() {
        outboxFailedCounter.increment();
    }

    public void incrementOutboxRetried() {
        outboxRetriedCounter.increment();
    }
}