package io.example.merchant_business.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantBusinessRequest {
    private Integer merchantBusinessInfoId;
    private String businessType;
    private String taxId;
    private Integer establishedYear;
    private Integer numberOfEmployees;
    private String websiteUrl;
}
