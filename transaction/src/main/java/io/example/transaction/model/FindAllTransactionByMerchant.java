package io.example.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindAllTransactionByMerchant {
    private Integer merchantId;
    private Integer page;
    private Integer pageSize;
    private String search;
}
