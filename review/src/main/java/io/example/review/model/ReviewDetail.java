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
public class ReviewDetail {
    private Long reviewDetailId;
    private Integer reviewId;
    private String type;
    private String url;
    private String caption;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("reviewDetailId", reviewDetailId)
                .put("reviewId", reviewId)
                .put("type", type)
                .put("url", url)
                .put("caption", caption);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static ReviewDetail fromJson(JsonObject json) {
        if (json == null)
            return null;
        return ReviewDetail.builder()
                .reviewDetailId(json.getLong("reviewDetailId"))
                .reviewId(json.getInteger("reviewId"))
                .type(json.getString("type"))
                .url(json.getString("url"))
                .caption(json.getString("caption"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static ReviewDetail fromRow(Row row) {
        if (row == null)
            return null;
        return ReviewDetail.builder()
                .reviewDetailId(row.getLong("review_detail_id"))
                .reviewId(row.getInteger("review_id"))
                .type(row.getString("type"))
                .url(row.getString("url"))
                .caption(row.getString("caption"))
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
