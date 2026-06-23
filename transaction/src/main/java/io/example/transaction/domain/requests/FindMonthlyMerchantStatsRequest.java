package io.example.transaction.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindMonthlyMerchantStatsRequest {
    private Integer merchantId;
    private int year;
    private int month;
}
