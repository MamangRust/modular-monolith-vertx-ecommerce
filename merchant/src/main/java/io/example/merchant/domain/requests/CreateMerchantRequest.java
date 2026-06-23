package io.example.merchant.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMerchantRequest {
    private Integer userId;
    private String name;
    private String apiKey;
    private String status;
}
