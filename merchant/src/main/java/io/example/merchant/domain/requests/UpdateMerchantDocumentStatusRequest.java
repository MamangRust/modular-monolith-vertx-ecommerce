package io.example.merchant.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMerchantDocumentStatusRequest {
    private Integer documentId;
    private String note;
    private String status;
}
