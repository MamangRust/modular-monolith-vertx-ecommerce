package io.example.merchant.service.kafka;

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

public class MerchantKafkaConsumerService {
  private static final Logger log = LoggerFactory.getLogger(MerchantKafkaConsumerService.class);

  private final KafkaConsumer<String, JsonObject> consumer;
  private final RedisService redisService;

  private static final String MERCHANT_CACHE_PREFIX = "merchant:";
  private static final String DOCUMENT_CACHE_PREFIX = "merchant_document:";

  public MerchantKafkaConsumerService(Vertx vertx, RedisService redisService, OpenTelemetry openTelemetry) {
    this.redisService = redisService;

    // NOTE: telemetry is initialized once by the owning verticle; the passed
    // openTelemetry instance is reused here instead of re-registering the global.

    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
    config.put("group.id", "merchant-service-group");
    config.put("auto.offset.reset", "earliest");

    this.consumer = KafkaConsumer.create(vertx, config);

    List<String> topics = Arrays.asList(
        "merchant-service-topic-transaction-event");

    consumer.handler(record -> {
      JsonObject event = record.value();
      String topic = record.topic();
      log.info("📥 Merchant service received event from topic {}: {}", topic, event.encode());
      handleEvent(topic, event);
    });

    consumer.subscribe(new java.util.HashSet<>(topics))
        .onSuccess(v -> log.info("📦 MerchantKafkaConsumerService successfully subscribed to topics: {}", topics))
        .onFailure(err -> log.error("❌ Failed to subscribe MerchantKafkaConsumerService to topics", err));
  }

  private void handleEvent(String topic, JsonObject event) {
    switch (topic) {
      case "merchant-service-topic-transaction-event" -> handleTransactionEvent(event);
      default -> log.warn("⚠️ Unknown topic: {}", topic);
    }
  }

  private void handleTransactionEvent(JsonObject event) {
    try {
      Integer merchantId = event.getInteger("merchantId");
      String status = event.getString("status");
      Integer amount = event.getInteger("amount");
      Integer transactionId = event.getInteger("transactionId");

      log.info("🔄 Processing transaction event for merchant {}: transaction={}, amount={}, status={}",
          merchantId, transactionId, amount, status);

      // Evict merchant-related caches so the next query fetches fresh data
      if (merchantId != null) {
        redisService.delete(MERCHANT_CACHE_PREFIX + "id:" + merchantId)
            .onSuccess(v -> log.info("✅ Evicted merchant cache for merchant {}", merchantId))
            .onFailure(err -> log.warn("⚠️ Failed to evict merchant cache for merchant {}", merchantId, err));

        redisService.deleteByPattern(MERCHANT_CACHE_PREFIX + "list:*")
            .onSuccess(v -> log.debug("✅ Evicted merchant list cache"))
            .onFailure(err -> log.warn("⚠️ Failed to evict merchant list cache", err));
      }

      log.info("✅ Successfully processed transaction event for merchant {}", merchantId);
    } catch (Exception e) {
      log.error("❌ Error processing transaction event: {}", event.encode(), e);
    }
  }

  public Future<Void> close() {
    if (consumer != null) {
      return consumer.close().onSuccess(v -> log.info("🔒 MerchantKafkaConsumerService closed"));
    }
    return Future.succeededFuture();
  }
}
