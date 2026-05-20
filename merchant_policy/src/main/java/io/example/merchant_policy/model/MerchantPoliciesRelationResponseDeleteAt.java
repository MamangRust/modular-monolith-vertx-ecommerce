package io.example.merchant_policy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPoliciesRelationResponseDeleteAt {
  private Long id;
  private Integer merchantId;
  private String policyType;
  private String title;
  private String description;
  private String merchantName;
  private String createdAt;
  private String updatedAt;
  private String deletedAt;

  public static MerchantPoliciesRelationResponseDeleteAt from(MerchantPolicyRelation entity) {
    if (entity == null) {
      return null;
    }
    return MerchantPoliciesRelationResponseDeleteAt.builder()
        .id(entity.getMerchantPolicyId() != null ? entity.getMerchantPolicyId() : 0L)
        .merchantId(entity.getMerchantId() != null ? entity.getMerchantId().intValue() : 0)
        .policyType(entity.getPolicyType() != null ? entity.getPolicyType() : "")
        .title(entity.getTitle() != null ? entity.getTitle() : "")
        .description(entity.getDescription() != null ? entity.getDescription() : "")
        .merchantName(entity.getMerchantName() != null ? entity.getMerchantName() : "")
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
        .deletedAt(entity.getDeletedAt() != null ? entity.getDeletedAt().toString() : "")
        .build();
  }
}
