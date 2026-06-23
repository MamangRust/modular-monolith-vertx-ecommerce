package io.example.merchant_business.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMerchantBusinessRequest {
    private Integer merchantId;
    private String businessType;
    private String taxId;
    private Integer establishedYear;
    private Integer numberOfEmployees;
    private String websiteUrl;
}
