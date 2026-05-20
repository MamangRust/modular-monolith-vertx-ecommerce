package io.example.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionYearlyMethod {
    private String year;
    private String paymentMethod;
    private Integer totalTransactions;
    private Long totalAmount;
}
