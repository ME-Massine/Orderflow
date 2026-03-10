package com.massine.orderflow.orderservice.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private final OutboxEventRepository repository;
    private final OutboxPublishingService publishingService;
    private final OutboxRetryPolicy retryPolicy;

    @Scheduled(fixedDelayString = "${orderflow.outbox.publisher-fixed-delay-ms:2000}")
    public void publishPendingEvents() {
        List<OutboxEvent> candidates =
                repository.findTop100ByStatusInOrderByCreatedAtAsc(
                        List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED)
                );

        if (candidates.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        for (OutboxEvent event : candidates) {
            if (retryPolicy.isEligible(event, now)) {
                publishingService.publish(event.getId());
            }
        }
    }
}