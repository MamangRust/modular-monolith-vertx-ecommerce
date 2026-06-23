package io.example.transaction.service.kafka;

import io.example.common.config.TelemetryConfig;
import io.example.common.service.RedisService;
import io.opentelemetry.api.OpenTelemetry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionKafkaConsumerService {
  private static final Logger log = LoggerFactory.getLogger(TransactionKafkaConsumerService.class);

  private final KafkaConsumer<String, JsonObject> consumer;
  private final RedisService redisService;
  private final TelemetryConfig telemetryConfig;

  private static final String CACHE_PREFIX = "transaction:";

  public TransactionKafkaConsumerService(Vertx vertx, RedisService redisService, OpenTelemetry openTelemetry) {
    this.redisService = redisService;

    JsonObject telConfig = new JsonObject().put("service.name", "transaction-kafka-consumer");
    this.telemetryConfig = new TelemetryConfig(telConfig);
    this.telemetryConfig.initialize();

    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
    config.put("group.id", "transaction-service-group");
    config.put("auto.offset.reset", "earliest");

    this.consumer = KafkaConsumer.create(vertx, config);

    List<String> topics = Arrays.asList(
        "transaction-service-topic-merchant-status-event");

    consumer.handler(record -> {
      JsonObject event = record.value();
      String topic = record.topic();
      log.info("📥 Transaction service received event from topic {}: {}", topic, event.encode());
      handleEvent(topic, event);
    });

    consumer.subscribe(new java.util.HashSet<>(topics))
        .onSuccess(v -> log.info("📦 TransactionKafkaConsumerService successfully subscribed to topics: {}", topics))
        .onFailure(err -> log.error("❌ Failed to subscribe TransactionKafkaConsumerService to topics", err));
  }

  private void handleEvent(String topic, JsonObject event) {
    switch (topic) {
      case "transaction-service-topic-merchant-status-event" -> handleMerchantStatusEvent(event);
      default -> log.warn("⚠️ Unknown topic: {}", topic);
    }
  }

  private void handleMerchantStatusEvent(JsonObject event) {
    try {
      Integer merchantId = event.getInteger("merchantId");
      String status = event.getString("status");

      log.info("🔄 Processing merchant status event: merchant={}, status={}", merchantId, status);

      // Evict transaction caches related to this merchant so fresh data is fetched
      if (merchantId != null) {
        redisService.deleteByPattern(CACHE_PREFIX + "*")
            .onSuccess(v -> log.info("✅ Evicted transaction cache due to merchant {} status change to {}", merchantId, status))
            .onFailure(err -> log.warn("⚠️ Failed to evict transaction cache", err));
      }

      log.info("✅ Successfully processed merchant status event for merchant {}", merchantId);
    } catch (Exception e) {
      log.error("❌ Error processing merchant status event: {}", event.encode(), e);
    }
  }

  public Future<Void> close() {
    if (consumer != null) {
      return consumer.close().onSuccess(v -> log.info("🔒 TransactionKafkaConsumerService closed"));
    }
    if (telemetryConfig != null) {
      telemetryConfig.shutdown();
    }
    return Future.succeededFuture();
  }
}
