package com.massine.orderflow.orderservice.messaging.consumer;

import com.massine.orderflow.orderservice.messaging.outbox.ProcessedEvent;
import com.massine.orderflow.orderservice.messaging.outbox.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class JpaIdempotencyStore implements IdempotencyStore {

    private final ProcessedEventRepository processedEventRepository;

    @Override
    public boolean markProcessed(UUID eventId) {
        try {
            processedEventRepository.save(
                    ProcessedEvent.builder()
                            .eventId(eventId)
                            .processedAt(Instant.now())
                            .build()
            );
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }
}