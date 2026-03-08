CREATE TABLE outbox_events (
                               id BIGSERIAL PRIMARY KEY,
                               event_id UUID NOT NULL,
                               aggregate_type VARCHAR(100) NOT NULL,
                               aggregate_id BIGINT NOT NULL,
                               event_type VARCHAR(150) NOT NULL,
                               payload TEXT NOT NULL,
                               status VARCHAR(20) NOT NULL,
                               attempt_count INT NOT NULL DEFAULT 0,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               published_at TIMESTAMP WITH TIME ZONE NULL,
                               last_attempt_at TIMESTAMP WITH TIME ZONE NULL,
                               failure_reason VARCHAR(2000) NULL,
                               CONSTRAINT uk_outbox_event_event_id UNIQUE (event_id)
);

CREATE INDEX idx_outbox_status_created_at
    ON outbox_events (status, created_at);

CREATE INDEX idx_outbox_aggregate_type_aggregate_id
    ON outbox_events (aggregate_type, aggregate_id);

CREATE TABLE processed_events (
                                  event_id UUID PRIMARY KEY,
                                  processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);