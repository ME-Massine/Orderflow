package com.massine.orderflow.orderservice.messaging.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherWorkerTest {

    @Mock
    private OutboxRetryPolicy retryPolicy;

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private OutboxPublishingService publishingService;

    @InjectMocks
    private OutboxPublisherWorker worker;

    @Test
    void shouldPublishAllEligiblePendingEvents() {
        OutboxEvent e1 = OutboxEvent.builder()
                .id(1L)
                .status(OutboxEventStatus.PENDING)
                .build();

        OutboxEvent e2 = OutboxEvent.builder()
                .id(2L)
                .status(OutboxEventStatus.PENDING)
                .build();

        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED)))
                .thenReturn(List.of(e1, e2));

        when(retryPolicy.isEligible(eq(e1), any())).thenReturn(true);
        when(retryPolicy.isEligible(eq(e2), any())).thenReturn(true);

        worker.publishPendingEvents();

        verify(publishingService).publish(1L);
        verify(publishingService).publish(2L);
    }

    @Test
    void shouldDoNothingWhenNoEvents() {
        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED)))
                .thenReturn(List.of());

        worker.publishPendingEvents();

        verifyNoInteractions(publishingService, retryPolicy);
    }

    @Test
    void shouldPublishEligibleFailedEvents() {
        OutboxEvent failed = OutboxEvent.builder()
                .id(1L)
                .status(OutboxEventStatus.FAILED)
                .build();

        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED)))
                .thenReturn(List.of(failed));

        when(retryPolicy.isEligible(eq(failed), any())).thenReturn(true);

        worker.publishPendingEvents();

        verify(publishingService).publish(1L);
    }

    @Test
    void shouldSkipFailedEventsStillInBackoffWindow() {
        OutboxEvent failed = OutboxEvent.builder()
                .id(1L)
                .status(OutboxEventStatus.FAILED)
                .build();

        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED)))
                .thenReturn(List.of(failed));

        when(retryPolicy.isEligible(eq(failed), any())).thenReturn(false);

        worker.publishPendingEvents();

        verifyNoInteractions(publishingService);
    }
}