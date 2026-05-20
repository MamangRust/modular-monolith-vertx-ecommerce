package io.example.review.model;

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
public class Review {
    private Long reviewId;
    private Integer userId;
    private Integer productId;
    private String name;
    private String comment;
    private Integer rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("reviewId", reviewId)
                .put("userId", userId)
                .put("productId", productId)
                .put("name", name)
                .put("comment", comment)
                .put("rating", rating);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static Review fromJson(JsonObject json) {
        if (json == null)
            return null;
        return Review.builder()
                .reviewId(json.getLong("reviewId"))
                .userId(json.getInteger("userId"))
                .productId(json.getInteger("productId"))
                .name(json.getString("name"))
                .comment(json.getString("comment"))
                .rating(json.getInteger("rating"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Review fromRow(Row row) {
        if (row == null)
            return null;
        return Review.builder()
                .reviewId(row.getLong("review_id"))
                .userId(row.getInteger("user_id"))
                .productId(row.getInteger("product_id"))
                .name(row.getString("name"))
                .comment(row.getString("comment"))
                .rating(row.getInteger("rating"))
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
