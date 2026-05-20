package io.example.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonthlyAmountFailed {
    private String year;
    private String month;
    private Integer totalFailed;
    private Integer totalAmount;
}
