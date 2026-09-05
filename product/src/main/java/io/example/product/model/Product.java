package io.example.product.model;

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
public class Product {
    private Long productId;
    private Integer merchantId;
    private Integer categoryId;
    private String name;
    private String description;
    private Integer price;
    private Integer countInStock;
    private String brand;
    private Integer weight;
    private Float rating;
    private String slugProduct;
    private String imageProduct;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("productId", productId)
                .put("merchantId", merchantId)
                .put("categoryId", categoryId)
                .put("name", name)
                .put("description", description)
                .put("price", price)
                .put("countInStock", countInStock)
                .put("brand", brand)
                .put("weight", weight)
                .put("rating", rating)
                .put("slugProduct", slugProduct)
                .put("imageProduct", imageProduct);
        if (createdAt != null)
            json.put("createdAt", createdAt.toString());
        if (updatedAt != null)
            json.put("updatedAt", updatedAt.toString());
        if (deletedAt != null)
            json.put("deletedAt", deletedAt.toString());
        return json;
    }

    public static Product fromJson(JsonObject json) {
        if (json == null)
            return null;
        return Product.builder()
                .productId(json.getLong("productId"))
                .merchantId(json.getInteger("merchantId"))
                .categoryId(json.getInteger("categoryId"))
                .name(json.getString("name"))
                .description(json.getString("description"))
                .price(json.getInteger("price"))
                .countInStock(json.getInteger("countInStock"))
                .brand(json.getString("brand"))
                .weight(json.getInteger("weight"))
                .rating(json.getFloat("rating"))
                .slugProduct(json.getString("slugProduct"))
                .imageProduct(json.getString("imageProduct"))
                .createdAt(parseTimestamp(json.getValue("createdAt")))
                .updatedAt(parseTimestamp(json.getValue("updatedAt")))
                .deletedAt(parseTimestamp(json.getValue("deletedAt")))
                .build();
    }

    public static Product fromRow(Row row) {
        if (row == null)
            return null;
        return Product.builder()
                .productId(row.getLong("product_id"))
                .merchantId(row.getInteger("merchant_id"))
                .categoryId(row.getInteger("category_id"))
                .name(row.getString("name"))
                .description(row.getString("description"))
                .price(row.getInteger("price"))
                .countInStock(row.getInteger("count_in_stock"))
                .brand(row.getString("brand"))
                .weight(row.getInteger("weight"))
                .rating(row.getFloat("rating"))
                .slugProduct(row.getString("slug_product"))
                .imageProduct(row.getString("image_product"))
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
