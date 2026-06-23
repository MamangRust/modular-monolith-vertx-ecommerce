package io.example.category.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindYearTotalPriceByMerchantRequest {
    private Integer year;
    private Integer merchantId;
}
