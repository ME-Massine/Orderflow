package com.massine.orderflow.orderservice.messaging.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxRetryPolicy {

    private final OutboxRetryProperties properties;

    public boolean isEligible(OutboxEvent event, Instant now) {
        if (event.getStatus() == OutboxEventStatus.PENDING) {
            return true;
        }

        if (event.getStatus() != OutboxEventStatus.FAILED) {
            return false;
        }

        if (event.getAttemptCount() >= properties.getMaxAttempts()) {
            return false;
        }

        if (event.getLastAttemptAt() == null) {
            return true;
        }

        long delayMs = computeDelayMs(event.getAttemptCount());
        Instant retryAt = event.getLastAttemptAt().plusMillis(delayMs);

        return !now.isBefore(retryAt);
    }

    long computeDelayMs(int attemptCount) {
        double delay = properties.getInitialDelayMs()
                * Math.pow(properties.getMultiplier(), Math.max(0, attemptCount - 1));

        return (long) delay;
    }
}