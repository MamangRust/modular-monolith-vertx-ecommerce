package io.example.merchant_business.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantBusinessResponseDeleteAt {
  private Integer id;
  private Integer merchantId;
  private String businessType;
  private String taxId;
  private Integer establishedYear;
  private Integer numberOfEmployees;
  private String websiteUrl;
  private String merchantName;
  private String createdAt;
  private String updatedAt;
  private String deletedAt;

  public static MerchantBusinessResponseDeleteAt from(MerchantBusiness m) {
    if (m == null) {
      return null;
    }
    return MerchantBusinessResponseDeleteAt.builder()
        .id(m.getMerchantBusinessInfoId() != null ? m.getMerchantBusinessInfoId().intValue() : 0)
        .merchantId(m.getMerchantId() != null ? m.getMerchantId() : 0)
        .businessType(m.getBusinessType() != null ? m.getBusinessType() : "")
        .taxId(m.getTaxId() != null ? m.getTaxId() : "")
        .establishedYear(m.getEstablishedYear() != null ? m.getEstablishedYear() : 0)
        .numberOfEmployees(m.getNumberOfEmployees() != null ? m.getNumberOfEmployees() : 0)
        .websiteUrl(m.getWebsiteUrl() != null ? m.getWebsiteUrl() : "")
        .merchantName(m.getMerchantName() != null ? m.getMerchantName() : "")
        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : "")
        .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : "")
        .deletedAt(m.getDeletedAt() != null ? m.getDeletedAt().toString() : null)
        .build();
  }
}
