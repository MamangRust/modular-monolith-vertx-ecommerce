package io.example.merchant.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMerchantDocumentRequest {
    private Integer merchantId;
    private String documentType;
    private String documentUrl;
}
