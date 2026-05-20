package io.example.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartCreateRecord {
    private Long productId;
    private Long userId;
    private String name;
    private Integer price;
    private String imageProduct;
    private Integer quantity;
    private Integer weight;
}
