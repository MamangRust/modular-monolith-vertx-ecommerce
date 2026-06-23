package io.example.product.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllProductMerchantRequest {
    private Long merchantId;
    private String search;
    private Long categoryId;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer page;
    private Integer pageSize;
}
