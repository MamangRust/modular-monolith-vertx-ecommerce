package io.example.transaction.repository.impl;

import io.example.transaction.repository.OutboxEvent;
import io.example.transaction.repository.OutboxRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {
    private final Pool client;

    @Override
    public Future<Void> save(String aggregateType, String aggregateId, String eventType,
                             JsonObject payload, String topic, String key) {
        return client.preparedQuery("""
                INSERT INTO outbox (aggregate_type, aggregate_id, event_type, payload, topic, key, created_at)
                VALUES ($1, $2, $3, $4::jsonb, $5, $6, NOW())
                ON CONFLICT (aggregate_type, aggregate_id, event_type, topic, key) DO NOTHING
                """)
                .execute(Tuple.of(aggregateType, aggregateId, eventType, payload.encode(), topic, key))
                .mapEmpty();
    }

    @Override
    public Future<List<OutboxEvent>> pollUnpublished(int limit) {
        // Claim rows atomically with a short lease. A plain SELECT ... FOR
        // UPDATE releases its lock as soon as the query completes, allowing
        // multiple publishers to send the same event concurrently.
        return client.preparedQuery("""
                WITH claimed AS (
                    UPDATE outbox
                    SET claimed_until = NOW() + INTERVAL '1 minute'
                    WHERE id IN (
                        SELECT id
                        FROM outbox
                        WHERE published_at IS NULL
                          AND (claimed_until IS NULL OR claimed_until < NOW())
                        ORDER BY created_at ASC
                        LIMIT $1
                        FOR UPDATE SKIP LOCKED
                    )
                    RETURNING id, aggregate_type, aggregate_id, event_type,
                              payload::text, topic, key, created_at, published_at
                )
                SELECT * FROM claimed ORDER BY created_at ASC
                """)
                .execute(Tuple.of(limit))
                .map(rows -> {
                    List<OutboxEvent> events = new ArrayList<>();
                    rows.forEach(row -> events.add(OutboxEvent.fromRow(row)));
                    return events;
                });
    }

    @Override
    public Future<Void> markPublished(long id) {
        return client.preparedQuery("""
                UPDATE outbox SET published_at = NOW(), claimed_until = NULL WHERE id = $1
                """)
                .execute(Tuple.of(id))
                .mapEmpty();
    }

    @Override
    public Future<Integer> deletePublishedOlderThan(Duration age) {
        return client.preparedQuery("""
                DELETE FROM outbox WHERE published_at IS NOT NULL AND published_at < NOW() - $1::interval
                """)
                .execute(Tuple.of(age.toMinutes() + " minutes"))
                .map(rows -> rows.rowCount());
    }
}
