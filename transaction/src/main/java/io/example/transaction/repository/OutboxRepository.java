package io.example.transaction.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface OutboxRepository {
    /**
     * Insert an outbox event. This should be called within the same
     * database transaction as the aggregate write.
     */
    Future<Void> save(String aggregateType, String aggregateId, String eventType,
                      JsonObject payload, String topic, String key);

    /**
     * Poll up to {@code limit} unpublished events in creation order.
     */
    Future<java.util.List<OutboxEvent>> pollUnpublished(int limit);

    /**
     * Mark an outbox event as published.
     */
    Future<Void> markPublished(long id);

    /**
     * Delete successfully published events older than the given age.
     */
    Future<Integer> deletePublishedOlderThan(java.time.Duration age);
}
