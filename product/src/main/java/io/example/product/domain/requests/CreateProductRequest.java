package io.example.product.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String description;
    private Integer price;
    private Integer countInStock;
    private String brand;
    private Integer weight;
    private Integer rating;
    private String slugProduct;
    private String imageProduct;
}
