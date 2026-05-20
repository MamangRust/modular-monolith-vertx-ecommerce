package io.example.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDocumentResponseDeleteAt {
  private Integer id;
  private Integer merchantId;
  private String documentType;
  private String documentUrl;
  private String status;
  private String note;
  private String uploadedAt;
  private String createdAt;
  private String updatedAt;
  private String deletedAt;

  public static MerchantDocumentResponseDeleteAt from(MerchantDocument d) {
    if (d == null) return null;
    return MerchantDocumentResponseDeleteAt.builder()
        .id(d.getDocumentId())
        .merchantId(d.getMerchantId())
        .documentType(d.getDocumentType() != null ? d.getDocumentType() : "")
        .documentUrl(d.getDocumentUrl() != null ? d.getDocumentUrl() : "")
        .status(d.getStatus() != null ? d.getStatus() : "")
        .note(d.getNote() != null ? d.getNote() : "")
        .uploadedAt(d.getUploadedAt() != null ? d.getUploadedAt().toInstant().toString() : "")
        .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toInstant().toString() : "")
        .updatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt().toInstant().toString() : "")
        .deletedAt(d.getDeletedAt() != null ? d.getDeletedAt().toInstant().toString() : null)
        .build();
  }
}
