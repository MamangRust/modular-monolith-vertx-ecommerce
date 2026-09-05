package io.example.transaction.repository;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    private long id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private JsonObject payload;
    private String topic;
    private String key;
    private Timestamp createdAt;
    private Timestamp publishedAt;

    public static OutboxEvent fromRow(Row row) {
        return OutboxEvent.builder()
                .id(row.getLong("id"))
                .aggregateType(row.getString("aggregate_type"))
                .aggregateId(row.getString("aggregate_id"))
                .eventType(row.getString("event_type"))
                .payload(parsePayload(row.getString("payload")))
                .topic(row.getString("topic"))
                .key(row.getString("key"))
                .createdAt(toTimestamp(row, "created_at"))
                .publishedAt(toTimestamp(row, "published_at"))
                .build();
    }

    /**
     * JSONB rows written by the current outbox use an object, but older
     * writers stored the encoded object as a JSON string. Accept both forms
     * so one malformed legacy row cannot stop the publisher forever.
     */
    private static JsonObject parsePayload(String raw) {
        if (raw == null || raw.isBlank()) {
            return new JsonObject();
        }
        try {
            return new JsonObject(raw);
        } catch (DecodeException firstDecodeFailure) {
            String nestedJson = io.vertx.core.json.Json.decodeValue(raw, String.class);
            return new JsonObject(nestedJson);
        }
    }

    private static Timestamp toTimestamp(Row row, String column) {
        LocalDateTime ldt = row.getLocalDateTime(column);
        return ldt != null ? Timestamp.valueOf(ldt) : null;
    }
}
