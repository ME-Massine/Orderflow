package com.massine.orderflow.orderservice.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    List<OutboxEvent> findTop100ByStatusInOrderByCreatedAtAsc(Collection<OutboxEventStatus> statuses);

    long countByStatus(OutboxEventStatus status);
}