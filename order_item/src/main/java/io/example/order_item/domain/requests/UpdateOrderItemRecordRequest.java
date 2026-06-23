package io.example.order_item.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderItemRecordRequest {
    private Integer orderItemId;
    private Integer quantity;
    private Integer price;
}
