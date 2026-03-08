package com.massine.orderflow.orderservice.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherWorker {

    private final OutboxEventRepository repository;
    private final OutboxPublishingService publishingService;

    @Scheduled(fixedDelayString = "${orderflow.outbox.publisher-fixed-delay-ms:2000}")
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishingService.publish(event.getId());
        }
    }
}