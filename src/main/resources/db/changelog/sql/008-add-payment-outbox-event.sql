-- liquibase formatted sql

-- changeset danila:1781694675706-1
CREATE TABLE payment_outbox_events
(
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);

-- changeset danila:1781694675706-2
CREATE INDEX IDX_PAYMENT_OUTBOX_EVENTS_STATUS_CREATED_AT
    ON payment_outbox_events (status, created_at);
