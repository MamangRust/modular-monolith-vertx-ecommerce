package io.example.order_item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDeleteAt {
    private Long id;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private Integer price;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static OrderItemResponseDeleteAt from(OrderItem o) {
        if (o == null) return null;
        return OrderItemResponseDeleteAt.builder()
                .id(o.getOrderItemId())
                .orderId(o.getOrderId())
                .productId(o.getProductId())
                .quantity(o.getQuantity())
                .price(o.getPrice())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toInstant().toString() : "")
                .updatedAt(o.getUpdatedAt() != null ? o.getUpdatedAt().toInstant().toString() : "")
                .deletedAt(o.getDeletedAt() != null ? o.getDeletedAt().toInstant().toString() : "")
                .build();
    }
}
