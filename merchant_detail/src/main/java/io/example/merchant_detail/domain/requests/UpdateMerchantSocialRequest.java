package io.example.merchant_detail.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantSocialRequest {
    private Integer id;
    private Integer merchantDetailId;
    private String platform;
    private String url;
}
