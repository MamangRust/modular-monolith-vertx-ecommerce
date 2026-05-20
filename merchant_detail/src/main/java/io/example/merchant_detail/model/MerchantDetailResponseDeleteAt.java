package io.example.merchant_detail.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetailResponseDeleteAt {
  private Long id;
  private Integer merchantId;
  private String displayName;
  private String coverImageUrl;
  private String logoUrl;
  private String shortDescription;
  private String websiteUrl;
  private List<MerchantSocialMediaLinkResponse> socialMediaLinks;
  private String createdAt;
  private String updatedAt;
  private String deletedAt;

  public static MerchantDetailResponseDeleteAt from(MerchantDetail entity) {
    if (entity == null) {
      return null;
    }
    return MerchantDetailResponseDeleteAt.builder()
        .id(entity.getMerchantDetailId())
        .merchantId(entity.getMerchantId() != null ? entity.getMerchantId() : 0)
        .displayName(entity.getDisplayName() != null ? entity.getDisplayName() : "")
        .coverImageUrl(entity.getCoverImageUrl() != null ? entity.getCoverImageUrl() : "")
        .logoUrl(entity.getLogoUrl() != null ? entity.getLogoUrl() : "")
        .shortDescription(entity.getShortDescription() != null ? entity.getShortDescription() : "")
        .websiteUrl(entity.getWebsiteUrl() != null ? entity.getWebsiteUrl() : "")
        .socialMediaLinks(new ArrayList<>())
        .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
        .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
        .deletedAt(entity.getDeletedAt() != null ? entity.getDeletedAt().toString() : "")
        .build();
  }

  public static MerchantDetailResponseDeleteAt from(MerchantDetailsRelation relation) {
    if (relation == null) {
      return null;
    }
    List<MerchantSocialMediaLinkResponse> links = new ArrayList<>();
    if (relation.getSocialMediaLinks() != null) {
      links = relation.getSocialMediaLinks().stream()
          .map(MerchantSocialMediaLinkResponse::from)
          .toList();
    }
    return MerchantDetailResponseDeleteAt.builder()
        .id(relation.getMerchantDetailId())
        .merchantId(relation.getMerchantId() != null ? relation.getMerchantId() : 0)
        .displayName(relation.getDisplayName() != null ? relation.getDisplayName() : "")
        .coverImageUrl(relation.getCoverImageUrl() != null ? relation.getCoverImageUrl() : "")
        .logoUrl(relation.getLogoUrl() != null ? relation.getLogoUrl() : "")
        .shortDescription(relation.getShortDescription() != null ? relation.getShortDescription() : "")
        .websiteUrl(relation.getWebsiteUrl() != null ? relation.getWebsiteUrl() : "")
        .socialMediaLinks(links)
        .createdAt(relation.getCreatedAt() != null ? relation.getCreatedAt().toString() : "")
        .updatedAt(relation.getUpdatedAt() != null ? relation.getUpdatedAt().toString() : "")
        .deletedAt(relation.getDeletedAt() != null ? relation.getDeletedAt().toString() : "")
        .build();
  }
}
