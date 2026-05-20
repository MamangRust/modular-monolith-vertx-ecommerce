package io.example.banner.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class Banner {
    private Long bannerId;
    private String name;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("bannerId", bannerId)
                .put("name", name)
                .put("isActive", isActive);

        if (startDate != null) json.put("startDate", startDate.toString());
        if (endDate != null) json.put("endDate", endDate.toString());
        if (startTime != null) json.put("startTime", startTime.toString());
        if (endTime != null) json.put("endTime", endTime.toString());
        if (createdAt != null) json.put("createdAt", createdAt.toString());
        if (updatedAt != null) json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null) json.put("deletedAt", deletedAt.toString());

        return json;
    }

    public static Banner fromJson(JsonObject json) {
        if (json == null) return null;
        return Banner.builder()
                .bannerId(json.getLong("bannerId"))
                .name(json.getString("name"))
                .startDate(parseDate(json.getValue("startDate")))
                .endDate(parseDate(json.getValue("endDate")))
                .startTime(parseTime(json.getValue("startTime")))
                .endTime(parseTime(json.getValue("endTime")))
                .isActive(json.getBoolean("isActive"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Banner fromRow(Row row) {
        if (row == null) return null;
        return Banner.builder()
                .bannerId(row.getLong("banner_id"))
                .name(row.getString("name"))
                .startDate(row.get(LocalDate.class, "start_date") != null ? Date.valueOf(row.get(LocalDate.class, "start_date")) : null)
                .endDate(row.get(LocalDate.class, "end_date") != null ? Date.valueOf(row.get(LocalDate.class, "end_date")) : null)
                .startTime(row.get(LocalTime.class, "start_time") != null ? Time.valueOf(row.get(LocalTime.class, "start_time")) : null)
                .endTime(row.get(LocalTime.class, "end_time") != null ? Time.valueOf(row.get(LocalTime.class, "end_time")) : null)
                .isActive(row.getBoolean("is_active"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(getTimestampFromRow(row, "deleted_at"))
                .build();
    }

    private static Date parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date) return (Date) value;
        if (value instanceof String) return Date.valueOf((String) value);
        return null;
    }

    private static Time parseTime(Object value) {
        if (value == null) return null;
        if (value instanceof Time) return (Time) value;
        if (value instanceof String) return Time.valueOf((String) value);
        return null;
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
        LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    @Override
    public String toString() {
        return toJson().encode();
    }
}
