package io.example.order.repository.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import com.google.protobuf.Empty;

import io.example.order.model.OrderItem;
import io.example.order.repository.OrderItemCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest;
import pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.VertxOrderItemCommandServiceGrpcClient;

@RequiredArgsConstructor
public class OrderItemCommandRepositoryImpl implements OrderItemCommandRepository {
    private final VertxOrderItemCommandServiceGrpcClient client;

    @Override
    public Future<OrderItem> createOrderItem(io.example.order.domain.requests.CreateOrderItemRecordRequest req) {
        CreateOrderItemRecordRequest request = CreateOrderItemRecordRequest.newBuilder()
                .setOrderId(req.getOrderId().intValue())
                .setProductId(req.getProductId().intValue())
                .setQuantity(req.getQuantity())
                .setPrice(req.getPrice())
                .build();

        return client.createOrderItem(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var item = response.getData();
                        return OrderItem.builder()
                                .orderItemId((long) item.getId())
                                .orderId(item.getOrderId())
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .createdAt(parseTimestamp(item.getCreatedAt()))
                                .updatedAt(parseTimestamp(item.getUpdatedAt()))
                                .build();
                    }
                    return null;
                });
    }

    @Override
    public Future<OrderItem> updateOrderItem(io.example.order.domain.requests.UpdateOrderItemRecordRequest req) {
        UpdateOrderItemRecordRequest request = UpdateOrderItemRecordRequest.newBuilder()
                .setOrderItemId(req.getOrderItemId().intValue())
                .setQuantity(req.getQuantity())
                .setPrice(req.getPrice())
                .build();

        return client.updateOrderItem(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var item = response.getData();
                        return OrderItem.builder()
                                .orderItemId((long) item.getId())
                                .orderId(item.getOrderId())
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .createdAt(parseTimestamp(item.getCreatedAt()))
                                .updatedAt(parseTimestamp(item.getUpdatedAt()))
                                .build();
                    }
                    return null;
                });
    }

    @Override
    public Future<Void> deleteOrderItemPermanently(Long orderId) {
        FindByIdOrderItemRequest request = FindByIdOrderItemRequest.newBuilder()
                .setId(orderId.intValue())
                .build();

        return client.deleteOrderItemByOrderPermanent(request)
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteOrderItemByIdPermanently(Long orderItemId) {
        FindByIdOrderItemRequest request = FindByIdOrderItemRequest.newBuilder()
                .setId(orderItemId.intValue())
                .build();

        return client.deleteOrderItemPermanent(request)
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteAllPermanentOrderItems() {
        return client.deleteAllPermanentOrdersItem(Empty.getDefaultInstance())
                .mapEmpty();
    }

    private Timestamp parseTimestamp(String ts) {
        if (ts == null || ts.isBlank())
            return null;
        try {
            return Timestamp.from(Instant.parse(ts));
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(ts.replace(" ", "T")));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
