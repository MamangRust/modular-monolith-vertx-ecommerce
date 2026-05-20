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
        .description("") // Default empty to fit schema
        .address("")     // Default empty to fit schema
        .contactEmail("") // Default empty to fit schema
        .contactPhone("") // Default empty to fit schema
        .status(m.getStatus() != null ? m.getStatus() : "")
        .createdAt(m.getCreatedAt() != null ? m.getCreatedAt().toInstant().toString() : "")
        .updatedAt(m.getUpdatedAt() != null ? m.getUpdatedAt().toInstant().toString() : "")
        .build();
  }
}
