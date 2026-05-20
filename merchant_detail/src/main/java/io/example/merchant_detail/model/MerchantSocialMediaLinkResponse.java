package io.example.merchant_detail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSocialMediaLinkResponse {
  private Long id;
  private Integer merchantDetailId;
  private String platform;
  private String url;
  private String createdAt;
  private String updatedAt;

  public static MerchantSocialMediaLinkResponse from(MerchantSocialMediaLink entity) {
    if (entity == null) {
      return null;
    }
    return MerchantSocialMediaLinkResponse.builder()
        .id(entity.getMerchantSocialId())
        .merchantDetailId(entity.getMerchantDetailId() != null ? entity.getMerchantDetailId() : 0)
        .platform(entity.getPlatform() != null ? entity.getPlatform() : "")
        .url(entity.getUrl() != null ? entity.getUrl() : "")
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
        .build();
  }
}
