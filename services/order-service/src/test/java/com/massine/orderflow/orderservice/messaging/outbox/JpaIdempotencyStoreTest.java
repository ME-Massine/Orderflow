package com.massine.orderflow.orderservice.messaging.outbox;

import com.massine.orderflow.orderservice.messaging.consumer.JpaIdempotencyStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JpaIdempotencyStoreTest {

    @Mock
    private ProcessedEventRepository repository;

    @InjectMocks
    private JpaIdempotencyStore store;

    @Test
    void isProcessedShouldReturnTrueWhenEventExists() {

        UUID eventId = UUID.randomUUID();

        when(repository.existsById(eventId)).thenReturn(true);

        boolean result = store.isProcessed(eventId);

        assertThat(result).isTrue();
        verify(repository).existsById(eventId);
    }

    @Test
    void isProcessedShouldReturnFalseWhenEventDoesNotExist() {

        UUID eventId = UUID.randomUUID();

        when(repository.existsById(eventId)).thenReturn(false);

        boolean result = store.isProcessed(eventId);

        assertThat(result).isFalse();
        verify(repository).existsById(eventId);
    }

    @Test
    void markProcessedShouldPersistEvent() {

        UUID eventId = UUID.randomUUID();

        store.markProcessed(eventId);

        verify(repository).save(any());
    }
}