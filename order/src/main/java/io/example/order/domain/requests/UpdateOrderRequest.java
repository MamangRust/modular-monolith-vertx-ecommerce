package io.example.order.domain.requests;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    private Long orderId;
    private Integer userId;
    private List<UpdateOrderItemRequest> items;
    private UpdateShippingAddressRequest shippingAddress;
}