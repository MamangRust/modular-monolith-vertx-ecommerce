package io.example.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Integer userId;
    private Integer productId;
    private String name;
    private Integer price;
    private String image;
    private Integer quantity;
    private Integer weight;
    private String createdAt;
    private String updatedAt;

    public static CartResponse from(Cart cart) {
        if (cart == null) return null;
        return CartResponse.builder()
                .id(cart.getCartId())
                .userId(cart.getUserId())
                .productId(cart.getProductId() != null ? cart.getProductId().intValue() : null)
                .name(cart.getName())
                .price(cart.getPrice())
                .image(cart.getImage())
                .quantity(cart.getQuantity())
                .weight(cart.getWeight())
                .createdAt(cart.getCreatedAt() != null ? cart.getCreatedAt().toString() : null)
                .updatedAt(cart.getUpdatedAt() != null ? cart.getUpdatedAt().toString() : null)
                .build();
    }
}
