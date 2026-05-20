package io.example.cart.model;

import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInfo {
    private Long productId;
    private String name;
    private Integer price;
    private String imageProduct;
    private Integer weight;

    public static ProductInfo fromRow(Row row) {
        if (row == null) return null;
        return ProductInfo.builder()
                .productId(row.getLong("product_id"))
                .name(row.getString("name"))
                .price(row.getInteger("price"))
                .imageProduct(row.getString("image_product"))
                .weight(row.getInteger("weight"))
                .build();
    }
}
