package io.example.order.model;

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
    private Integer countInStock;
}
