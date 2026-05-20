package io.example.order_item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private Integer price;
    private String createdAt;
    private String updatedAt;

    public static OrderItemResponse from(OrderItem o) {
        if (o == null) return null;
        return OrderItemResponse.builder()
                .id(o.getOrderItemId())
                .orderId(o.getOrderId())
                .productId(o.getProductId())
                .quantity(o.getQuantity())
                .price(o.getPrice())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toInstant().toString() : "")
                .updatedAt(o.getUpdatedAt() != null ? o.getUpdatedAt().toInstant().toString() : "")
                .build();
    }
}
