-- Outbox table for transactional message delivery
-- This table lives in the ecommerce_transaction database.
-- Events are written in the same DB transaction as the aggregate,
-- then a background publisher polls and sends them to Kafka.
CREATE TABLE IF NOT EXISTS outbox (
    id              SERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(50)  NOT NULL,  -- e.g. 'transaction'
    aggregate_id    VARCHAR(50)  NOT NULL,  -- e.g. transaction_id
    event_type      VARCHAR(100) NOT NULL,  -- e.g. 'transaction.created'
    payload         JSONB        NOT NULL,
    topic           VARCHAR(100) NOT NULL,  -- Kafka topic
    key             VARCHAR(255) NOT NULL,  -- Kafka message key
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP    DEFAULT NULL,
    claimed_until   TIMESTAMP    DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox (created_at ASC)
    WHERE published_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_outbox_event_destination
    ON outbox (aggregate_type, aggregate_id, event_type, topic, key);
