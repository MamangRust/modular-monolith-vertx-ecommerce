package io.example.transaction.service.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.service.KafkaService;
import io.example.transaction.repository.OutboxRepository;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * Background worker that polls the outbox table for unpublished events
 * and delivers them to Kafka. Runs every {@code pollIntervalMs} milliseconds.
 *
 * <p>This pattern guarantees at-least-once delivery: events are only
 * marked as published after Kafka acknowledges receipt. If the process
 * crashes between publishing and marking, the event will be retried
 * (idempotent consumers should handle duplicates).
 */
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private static final int DEFAULT_POLL_INTERVAL_MS = 5_000;
    private static final int DEFAULT_BATCH_SIZE = 50;

    private final OutboxRepository outboxRepo;
    private final KafkaService kafkaService;
    private final Vertx vertx;
    private final int pollIntervalMs;
    private final int batchSize;

    private long timerId = -1;

    public OutboxPublisher(OutboxRepository outboxRepo, KafkaService kafkaService, Vertx vertx) {
        this(outboxRepo, kafkaService, vertx, DEFAULT_POLL_INTERVAL_MS, DEFAULT_BATCH_SIZE);
    }

    public OutboxPublisher(OutboxRepository outboxRepo, KafkaService kafkaService, Vertx vertx,
                           int pollIntervalMs, int batchSize) {
        this.outboxRepo = outboxRepo;
        this.kafkaService = kafkaService;
        this.vertx = vertx;
        this.pollIntervalMs = pollIntervalMs;
        this.batchSize = batchSize;
    }

    /**
     * Start the polling loop. Idempotent — safe to call multiple times.
     */
    public void start() {
        if (timerId != -1) {
            return; // already running
        }
        log.info("OutboxPublisher started (poll every {} ms, batch size {})", pollIntervalMs, batchSize);
        scheduleNext();
    }

    /**
     * Stop the polling loop. Idempotent.
     */
    public void stop() {
        if (timerId != -1) {
            vertx.cancelTimer(timerId);
            timerId = -1;
            log.info("OutboxPublisher stopped");
        }
    }

    private void scheduleNext() {
        timerId = vertx.setTimer(pollIntervalMs, id -> {
            pollAndPublish()
                    .onComplete(ar -> {
                        if (ar.failed()) {
                            log.error("OutboxPublisher poll cycle failed", ar.cause());
                        }
                        scheduleNext();
                    });
        });
    }

    Future<Void> pollAndPublish() {
        return outboxRepo.pollUnpublished(batchSize)
                .compose(events -> {
                    if (events.isEmpty()) {
                        return Future.succeededFuture();
                    }
                    log.debug("OutboxPublisher: publishing {} events", events.size());

                    Future<Void> chain = Future.succeededFuture();
                    for (var event : events) {
                        chain = chain
                                .compose(v -> kafkaService.sendMessage(
                                        event.getTopic(), event.getKey(), event.getPayload()))
                                .compose(v -> outboxRepo.markPublished(event.getId()))
                                .recover(err -> {
                                    log.error("Failed to publish outbox event id={} (will retry): {}",
                                            event.getId(), err.getMessage());
                                    return Future.succeededFuture(); // continue with next
                                });
                    }
                    return chain;
                });
    }
}
