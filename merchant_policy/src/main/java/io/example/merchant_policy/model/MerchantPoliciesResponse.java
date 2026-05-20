package io.example.merchant_policy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPoliciesResponse {
  private Long id;
  private Integer merchantId;
  private String policyType;
  private String title;
  private String description;
  private String createdAt;
  private String updatedAt;
  private String merchantName;

  public static MerchantPoliciesResponse from(MerchantPolicy entity) {
    if (entity == null) {
      return null;
    }
    return MerchantPoliciesResponse.builder()
        .id(entity.getMerchantPolicyId() != null ? entity.getMerchantPolicyId() : 0L)
        .merchantId(entity.getMerchantId() != null ? entity.getMerchantId() : 0)
        .policyType(entity.getPolicyType() != null ? entity.getPolicyType() : "")
        .title(entity.getTitle() != null ? entity.getTitle() : "")
        .description(entity.getDescription() != null ? entity.getDescription() : "")
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
        .merchantName("")
        .build();
  }
}
