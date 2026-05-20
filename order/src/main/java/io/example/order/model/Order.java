package io.example.order.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long orderId;
    private Integer userId;
    private Integer merchantId;
    private Integer totalPrice;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("orderId", orderId)
                .put("userId", userId)
                .put("merchantId", merchantId)
                .put("totalPrice", totalPrice);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static Order fromJson(JsonObject json) {
        if (json == null)
            return null;
        return Order.builder()
                .orderId(json.getLong("orderId"))
                .userId(json.getInteger("userId"))
                .merchantId(json.getInteger("merchantId"))
                .totalPrice(json.getInteger("totalPrice"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Order fromRow(Row row) {
        if (row == null)
            return null;
        return Order.builder()
                .orderId(row.getLong("order_id"))
                .userId(row.getInteger("user_id"))
                .merchantId(row.getInteger("merchant_id"))
                .totalPrice(row.getInteger("total_price"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(getTimestampFromRow(row, "deleted_at"))
                .build();
    }

    private static Timestamp parseTimestamp(Object value) {
        if (value == null)
            return null;
        if (value instanceof Timestamp)
            return (Timestamp) value;
        if (value instanceof String) {
            try {
                return Timestamp.from(Instant.parse((String) value));
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private static Timestamp getTimestampFromRow(Row row, String column) {
        LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    @Override
    public String toString() {
        return toJson().encode();
    }
}
