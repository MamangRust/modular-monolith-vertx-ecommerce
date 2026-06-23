package io.example.cart.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCartRequest {
    private Integer quantity;
    private Long productId;
    private Integer userId;
}