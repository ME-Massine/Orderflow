package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.messaging.outbox.ProcessedEvent;
import com.massine.orderflow.orderservice.messaging.outbox.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class JpaIdempotencyStore implements IdempotencyStore {

    private final ProcessedEventRepository processedEventRepository;

    @Override
    public boolean isProcessed(UUID eventId) {
        return processedEventRepository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId) {
        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(Instant.now())
                        .build()
        );
    }
}