package io.example.merchant_award.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantAwardResponseDeleteAt {
  private Integer id;
  private Integer merchantId;
  private String title;
  private String description;
  private String issuedBy;
  private String issueDate;
  private String expiryDate;
  private String certificateUrl;
  private String createdAt;
  private String updatedAt;
  private String merchantName;
  private String deletedAt;

  public static MerchantAwardResponseDeleteAt from(MerchantAward mca) {
    if (mca == null) {
      return null;
    }
    return MerchantAwardResponseDeleteAt.builder()
        .id(mca.getMerchantCertificationId() != null ? mca.getMerchantCertificationId().intValue() : 0)
        .merchantId(mca.getMerchantId() != null ? mca.getMerchantId() : 0)
        .title(mca.getTitle() != null ? mca.getTitle() : "")
        .description(mca.getDescription() != null ? mca.getDescription() : "")
        .issuedBy(mca.getIssuedBy() != null ? mca.getIssuedBy() : "")
        .issueDate(mca.getIssueDate() != null ? mca.getIssueDate().toString() : "")
        .expiryDate(mca.getExpiryDate() != null ? mca.getExpiryDate().toString() : "")
        .certificateUrl(mca.getCertificateUrl() != null ? mca.getCertificateUrl() : "")
        .createdAt(mca.getCreatedAt() != null ? mca.getCreatedAt().toInstant().toString() : "")
        .updatedAt(mca.getUpdatedAt() != null ? mca.getUpdatedAt().toInstant().toString() : "")
        .merchantName(mca.getMerchantName() != null ? mca.getMerchantName() : "")
        .deletedAt(mca.getDeletedAt() != null ? mca.getDeletedAt().toInstant().toString() : "")
        .build();
  }
}
