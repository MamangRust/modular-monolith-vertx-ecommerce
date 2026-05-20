package io.example.order_item.model;

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
public class OrderItem {
    private Long orderItemId;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private Integer price;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("orderItemId", orderItemId)
                .put("orderId", orderId)
                .put("productId", productId)
                .put("quantity", quantity)
                .put("price", price);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static OrderItem fromJson(JsonObject json) {
        if (json == null)
            return null;
        return OrderItem.builder()
                .orderItemId(json.getLong("orderItemId"))
                .orderId(json.getInteger("orderId"))
                .productId(json.getInteger("productId"))
                .quantity(json.getInteger("quantity"))
                .price(json.getInteger("price"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static OrderItem fromRow(Row row) {
        if (row == null)
            return null;
        return OrderItem.builder()
                .orderItemId(row.getLong("order_item_id"))
                .orderId(row.getInteger("order_id"))
                .productId(row.getInteger("product_id"))
                .quantity(row.getInteger("quantity"))
                .price(row.getInteger("price"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(row.getColumnIndex("deleted_at") != -1 ? getTimestampFromRow(row, "deleted_at") : null)
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
        try {
            LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
            return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return toJson().encode();
    }
}
