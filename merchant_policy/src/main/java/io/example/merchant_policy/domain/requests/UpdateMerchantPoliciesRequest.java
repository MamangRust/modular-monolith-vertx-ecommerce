package io.example.merchant_policy.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantPoliciesRequest {
    private Integer merchantPolicyId;
    private String policyType;
    private String title;
    private String description;
}
