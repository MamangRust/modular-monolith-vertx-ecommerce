package io.example.shipping_address.model;

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
public class ShippingAddress {
    private Long shippingAddressId;
    private Integer orderId;
    private String alamat;
    private String provinsi;
    private String negara;
    private String kota;
    private String courier;
    private String shippingMethod;
    private Integer shippingCost;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("shippingAddressId", shippingAddressId)
                .put("orderId", orderId)
                .put("alamat", alamat)
                .put("provinsi", provinsi)
                .put("negara", negara)
                .put("kota", kota)
                .put("courier", courier)
                .put("shippingMethod", shippingMethod)
                .put("shippingCost", shippingCost);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static ShippingAddress fromJson(JsonObject json) {
        if (json == null)
            return null;
        return ShippingAddress.builder()
                .shippingAddressId(json.getLong("shippingAddressId"))
                .orderId(json.getInteger("orderId"))
                .alamat(json.getString("alamat"))
                .provinsi(json.getString("provinsi"))
                .negara(json.getString("negara"))
                .kota(json.getString("kota"))
                .courier(json.getString("courier"))
                .shippingMethod(json.getString("shippingMethod"))
                .shippingCost(json.getInteger("shippingCost"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static ShippingAddress fromRow(Row row) {
        if (row == null)
            return null;
        
        // Handle postgres columns which can have deleted_at
        Timestamp deletedAtVal = null;
        try {
            deletedAtVal = getTimestampFromRow(row, "deleted_at");
        } catch (Exception ignored) {}

        return ShippingAddress.builder()
                .shippingAddressId(row.getLong("shipping_address_id"))
                .orderId(row.getInteger("order_id"))
                .alamat(row.getString("alamat"))
                .provinsi(row.getString("provinsi"))
                .negara(row.getString("negara"))
                .kota(row.getString("kota"))
                .courier(row.getString("courier"))
                .shippingMethod(row.getString("shipping_method"))
                .shippingCost(row.getInteger("shipping_cost"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(deletedAtVal)
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
