package io.example.merchant_detail.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantDetailRequest {
    private Integer merchantDetailId;
    private String displayName;
    private String coverImageUrl;
    private String logoUrl;
    private String shortDescription;
    private String websiteUrl;
}
