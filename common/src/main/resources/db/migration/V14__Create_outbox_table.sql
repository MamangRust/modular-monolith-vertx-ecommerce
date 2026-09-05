CREATE TABLE IF NOT EXISTS outbox (
    id              SERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    VARCHAR(50)  NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB        NOT NULL,
    topic           VARCHAR(100) NOT NULL,
    key             VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP    DEFAULT NULL,
    claimed_until   TIMESTAMP    DEFAULT NULL
);

ALTER TABLE outbox ADD COLUMN IF NOT EXISTS claimed_until TIMESTAMP DEFAULT NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox (created_at ASC) WHERE published_at IS NULL;

-- A transaction replay must repair missing outbox rows without duplicating
-- events that were already persisted for the same destination.
CREATE UNIQUE INDEX IF NOT EXISTS uq_outbox_event_destination
    ON outbox (aggregate_type, aggregate_id, event_type, topic, key);
