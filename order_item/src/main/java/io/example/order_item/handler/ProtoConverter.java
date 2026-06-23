package io.example.order_item.handler;

import com.google.protobuf.StringValue;

import io.example.order_item.model.OrderItem;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;

public class ProtoConverter {

    public static pb.order_item.OrderItemCommon.OrderItemResponse toProto(OrderItem o) {
        if (o == null)
            return pb.order_item.OrderItemCommon.OrderItemResponse.getDefaultInstance();
        return pb.order_item.OrderItemCommon.OrderItemResponse.newBuilder()
                .setId(o.getOrderItemId() != null ? o.getOrderItemId().intValue() : 0)
                .setOrderId(o.getOrderId() != null ? o.getOrderId() : 0)
                .setProductId(o.getProductId() != null ? o.getProductId() : 0)
                .setQuantity(o.getQuantity() != null ? o.getQuantity() : 0)
                .setPrice(o.getPrice() != null ? o.getPrice() : 0)
                .setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "")
                .setUpdatedAt(o.getUpdatedAt() != null ? o.getUpdatedAt().toString() : "")
                .build();
    }

    public static pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt toProtoDeleteAt(OrderItem o) {
        if (o == null)
            return pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt.getDefaultInstance();
        pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt.Builder b = pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt
                .newBuilder()
                .setId(o.getOrderItemId() != null ? o.getOrderItemId().intValue() : 0)
                .setOrderId(o.getOrderId() != null ? o.getOrderId() : 0)
                .setProductId(o.getProductId() != null ? o.getProductId() : 0)
                .setQuantity(o.getQuantity() != null ? o.getQuantity() : 0)
                .setPrice(o.getPrice() != null ? o.getPrice() : 0)
                .setCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "")
                .setUpdatedAt(o.getUpdatedAt() != null ? o.getUpdatedAt().toString() : "");
        if (o.getDeletedAt() != null) {
            b.setDeletedAt(StringValue.of(o.getDeletedAt().toString()));
        }
        return b.build();
    }

    public static pb.order_item.OrderItemCommon.OrderItemResponse fromModel(OrderItemResponse r) {
        if (r == null)
            return pb.order_item.OrderItemCommon.OrderItemResponse.getDefaultInstance();
        return pb.order_item.OrderItemCommon.OrderItemResponse.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setOrderId(r.getOrderId() != null ? r.getOrderId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setQuantity(r.getQuantity() != null ? r.getQuantity() : 0)
                .setPrice(r.getPrice() != null ? r.getPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt fromModelDeleteAt(
            OrderItemResponseDeleteAt r) {
        if (r == null)
            return pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt.getDefaultInstance();
        pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt.Builder b = pb.order_item.OrderItemCommon.OrderItemResponseDeleteAt
                .newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setOrderId(r.getOrderId() != null ? r.getOrderId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setQuantity(r.getQuantity() != null ? r.getQuantity() : 0)
                .setPrice(r.getPrice() != null ? r.getPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");
        if (r.getDeletedAt() != null && !r.getDeletedAt().isEmpty()) {
            b.setDeletedAt(StringValue.of(r.getDeletedAt()));
        }
        return b.build();
    }

    public static pb.order_item.OrderItemCommon.OrderItemResponse fromModelDeleteAtToResponse(
            OrderItemResponseDeleteAt r) {
        if (r == null)
            return pb.order_item.OrderItemCommon.OrderItemResponse.getDefaultInstance();
        return pb.order_item.OrderItemCommon.OrderItemResponse.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setOrderId(r.getOrderId() != null ? r.getOrderId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setQuantity(r.getQuantity() != null ? r.getQuantity() : 0)
                .setPrice(r.getPrice() != null ? r.getPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }
}
