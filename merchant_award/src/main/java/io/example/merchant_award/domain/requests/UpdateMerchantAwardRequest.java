package io.example.merchant_award.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantAwardRequest {
    private Long merchantCertificationId;
    private String title;
    private String description;
    private String issuedBy;
    private String issueDate;
    private String expiryDate;
    private String certificateUrl;
}
