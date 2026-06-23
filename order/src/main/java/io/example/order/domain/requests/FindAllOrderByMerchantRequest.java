package io.example.order.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllOrderByMerchantRequest {
    private Long merchantId;
    private String search;
    private int page;
    private int pageSize;
}