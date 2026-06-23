package io.example.merchant.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantDocumentRequest {
    private Integer documentId;
    private Integer merchantId;
    private String documentType;
    private String documentUrl;
    private String note;
    private String status;
}
