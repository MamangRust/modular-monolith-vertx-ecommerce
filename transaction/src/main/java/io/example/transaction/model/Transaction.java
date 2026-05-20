package io.example.transaction.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import io.example.transaction.enums.PaymentStatus;

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
public class Transaction {
    private Long transactionId;
    private Integer orderId;
    private Integer merchantId;
    private String paymentMethod;
    private Integer amount;
    private PaymentStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("transactionId", transactionId)
                .put("orderId", orderId)
                .put("merchantId", merchantId)
                .put("paymentMethod", paymentMethod)
                .put("amount", amount)
                .put("status", status != null ? status.name() : null);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static Transaction fromJson(JsonObject json) {
        if (json == null)
            return null;
        PaymentStatus stat = null;
        try {
            if (json.getString("status") != null)
                stat = PaymentStatus.valueOf(json.getString("status").toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return Transaction.builder()
                .transactionId(json.getLong("transactionId"))
                .orderId(json.getInteger("orderId"))
                .merchantId(json.getInteger("merchantId"))
                .paymentMethod(json.getString("paymentMethod"))
                .amount(json.getInteger("amount"))
                .status(stat)
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Transaction fromRow(Row row) {
        if (row == null)
            return null;
        PaymentStatus stat = null;
        try {
            if (row.getString("payment_status") != null) {
                String val = row.getString("payment_status").toUpperCase();
                if ("SUCCESS".equals(val)) {
                    stat = PaymentStatus.PAID;
                } else {
                    stat = PaymentStatus.valueOf(val);
                }
            }
        } catch (IllegalArgumentException ignored) {
        }

        return Transaction.builder()
                .transactionId(row.getLong("transaction_id"))
                .orderId(row.getInteger("order_id"))
                .merchantId(row.getInteger("merchant_id"))
                .paymentMethod(row.getString("payment_method"))
                .amount(row.getInteger("amount"))
                .status(stat)
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
