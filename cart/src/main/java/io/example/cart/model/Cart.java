package io.example.cart.model;

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
public class Cart {
    private Long cartId;
    private Integer userId;
    private Long productId; 
    private String name;
    private Integer price;
    private String image;
    private Integer quantity;
    private Integer weight;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("cartId", cartId)
                .put("userId", userId)
                .put("productId", productId)
                .put("name", name)
                .put("price", price)
                .put("image", image)
                .put("quantity", quantity)
                .put("weight", weight);

        if (createdAt != null) json.put("createdAt", createdAt.toString());
        if (updatedAt != null) json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null) json.put("deletedAt", deletedAt.toString());

        return json;
    }

    public static Cart fromJson(JsonObject json) {
        if (json == null) return null;
        return Cart.builder()
                .cartId(json.getLong("cartId"))
                .userId(json.getInteger("userId"))
                .productId(json.getLong("productId"))
                .name(json.getString("name"))
                .price(json.getInteger("price"))
                .image(json.getString("image"))
                .quantity(json.getInteger("quantity"))
                .weight(json.getInteger("weight"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Cart fromRow(Row row) {
        if (row == null) return null;
        return Cart.builder()
                .cartId(row.getLong("cart_id"))
                .userId(row.getInteger("user_id"))
                .productId(row.getLong("product_id"))
                .name(row.getString("name"))
                .price(row.getInteger("price"))
                .image(row.getString("image"))
                .quantity(row.getInteger("quantity"))
                .weight(row.getInteger("weight"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(getTimestampFromRow(row, "deleted_at"))
                .build();
    }

    private static Timestamp parseTimestamp(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) return (Timestamp) value;
        if (value instanceof String) {
            try { return Timestamp.from(Instant.parse((String) value)); } 
            catch (DateTimeParseException e) { return null; }
        }
        return null;
    }

    private static Timestamp getTimestampFromRow(Row row, String column) {
        try {
            if (row.getColumnIndex(column) < 0) return null;
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
