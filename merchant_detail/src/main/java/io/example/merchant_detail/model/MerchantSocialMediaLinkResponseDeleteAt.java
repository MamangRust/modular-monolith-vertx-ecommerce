package io.example.merchant_detail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSocialMediaLinkResponseDeleteAt {
    private Long id;
    private Integer merchantDetailId;
    private String platform;
    private String url;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static MerchantSocialMediaLinkResponseDeleteAt from(MerchantSocialMediaLink entity) {
        if (entity == null) {
            return null;
        }
        return MerchantSocialMediaLinkResponseDeleteAt.builder()
                .id(entity.getMerchantSocialId())
                .merchantDetailId(entity.getMerchantDetailId() != null ? entity.getMerchantDetailId() : 0)
                .platform(entity.getPlatform() != null ? entity.getPlatform() : "")
                .url(entity.getUrl() != null ? entity.getUrl() : "")
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : "")
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : "")
                .deletedAt(entity.getDeletedAt() != null ? entity.getDeletedAt().toString() : "")
                .build();
    }
}
