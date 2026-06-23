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
public class CreateOrderRequest {
    private Long merchantId;
    private Integer userId;
    private List<CreateOrderItemRequest> items;
    private CreateShippingAddressRequest shippingAddress;
}