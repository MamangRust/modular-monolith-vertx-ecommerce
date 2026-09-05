package io.example.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantResponse {
  private Integer id;
  private Integer userId;
  private String name;
  private String description;
  private String address;
  private String contactEmail;
  private String contactPhone;
  private String status;
  private String createdAt;
  private String updatedAt;

  public static MerchantResponse from(Merchant m) {
    if (m == null) return null;
    return MerchantResponse.builder()
        .id(m.getMerchantId())
        .userId(m.getUserId())
        .name(m.getName() != null ? m.getName() : "")
        .description(m.getDescription() != null ? m.getDescription() : "")
        .address(m.getAddress() != null ? m.getAddress() : "")
        .contactEmail(m.getContactEmail() != null ? m.getContactEmail() : "")
        .contactPhone(m.getContactPhone() != null ? m.getContactPhone() : "")
        .status(m.getStatus() != null ? m.getStatus() : "")
        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toInstant().toString() : "")
        .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().toInstant().toString() : "")
        .build();
  }
}
