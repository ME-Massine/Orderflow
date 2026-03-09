package com.massine.orderflow.orderservice.messaging.consumer.outbox;

import com.massine.orderflow.orderservice.messaging.consumer.JpaIdempotencyStore;
import com.massine.orderflow.orderservice.messaging.outbox.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class JpaIdempotencyStoreTest {

    @Mock
    private ProcessedEventRepository repository;

    @InjectMocks
    private JpaIdempotencyStore store;

    @Test
    void shouldReturnTrueWhenInsertSucceeds() {

        boolean result = store.markProcessed(UUID.randomUUID());

        assertThat(result).isTrue();
        verify(repository).save(any());
    }

    @Test
    void shouldReturnFalseOnDuplicate() {

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(repository).save(any());

        boolean result = store.markProcessed(UUID.randomUUID());

        assertThat(result).isFalse();
    }
}