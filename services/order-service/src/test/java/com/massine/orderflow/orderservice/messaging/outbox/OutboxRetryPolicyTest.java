package com.massine.orderflow.orderservice.messaging.consumer.outbox;

import com.massine.orderflow.orderservice.messaging.outbox.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxRetryPolicyTest {

    @Test
    void pendingEvent_shouldAlwaysBeEligible() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        OutboxEvent event = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .status(OutboxEventStatus.PENDING)
                .build();

        assertThat(policy.isEligible(event, Instant.now())).isTrue();
    }

    @Test
    void publishedEvent_shouldNeverBeEligible() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        OutboxEvent event = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .status(OutboxEventStatus.PUBLISHED)
                .build();

        assertThat(policy.isEligible(event, Instant.now())).isFalse();
    }

    @Test
    void failedEvent_shouldNotBeEligible_whenMaxAttemptsReached() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        properties.setMaxAttempts(3);

        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        OutboxEvent event = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .status(OutboxEventStatus.FAILED)
                .attemptCount(3)
                .build();

        assertThat(policy.isEligible(event, Instant.now())).isFalse();
    }

    @Test
    void failedEvent_shouldNotBeEligible_beforeBackoffDelayElapsed() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        properties.setInitialDelayMs(5000);
        properties.setMultiplier(2.0);

        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        Instant lastAttempt = Instant.now().minusMillis(2000);

        OutboxEvent event = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .status(OutboxEventStatus.FAILED)
                .attemptCount(1)
                .lastAttemptAt(lastAttempt)
                .build();

        assertThat(policy.isEligible(event, Instant.now())).isFalse();
    }

    @Test
    void failedEvent_shouldBeEligible_afterBackoffDelayElapsed() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        properties.setInitialDelayMs(5000);
        properties.setMultiplier(2.0);

        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        Instant lastAttempt = Instant.now().minusMillis(6000);

        OutboxEvent event = OutboxEvent.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .status(OutboxEventStatus.FAILED)
                .attemptCount(1)
                .lastAttemptAt(lastAttempt)
                .build();

        assertThat(policy.isEligible(event, Instant.now())).isTrue();
    }

    @Test
    void computeDelayMs_shouldApplyExponentialBackoff() {
        OutboxRetryProperties properties = new OutboxRetryProperties();
        properties.setInitialDelayMs(5000);
        properties.setMultiplier(2.0);

        OutboxRetryPolicy policy = new OutboxRetryPolicy(properties);

        assertThat(policy.computeDelayMs(1)).isEqualTo(5000);
        assertThat(policy.computeDelayMs(2)).isEqualTo(10000);
        assertThat(policy.computeDelayMs(3)).isEqualTo(20000);
    }
}