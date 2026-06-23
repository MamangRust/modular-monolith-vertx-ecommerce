package io.example.transaction.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    private Long orderID;
    private Long merchantID;
    private String paymentMethod;
    private Integer amount;
    private String paymentStatus;
}
