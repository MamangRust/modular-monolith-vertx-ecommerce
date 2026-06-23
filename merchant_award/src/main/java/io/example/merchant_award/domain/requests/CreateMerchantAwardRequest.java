package io.example.merchant_award.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMerchantAwardRequest {
    private Integer merchantId;
    private String title;
    private String description;
    private String issuedBy;
    private String issueDate;
    private String expiryDate;
    private String certificateUrl;
}
