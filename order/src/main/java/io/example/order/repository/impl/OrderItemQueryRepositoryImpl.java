package io.example.order.repository.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.example.order.model.OrderItem;
import io.example.order.repository.OrderItemQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.OrderItemCommand.CalculateTotalPriceRequest;
import pb.order_item.VertxOrderItemQueryServiceGrpcClient;
import pb.order_item.VertxOrderItemCommandServiceGrpcClient;

@RequiredArgsConstructor
public class OrderItemQueryRepositoryImpl implements OrderItemQueryRepository {
    private final VertxOrderItemQueryServiceGrpcClient queryClient;
    private final VertxOrderItemCommandServiceGrpcClient commandClient;

    @Override
    public Future<List<OrderItem>> getOrderItemsByOrder(Integer orderId) {
        FindByIdOrderItemRequest request = FindByIdOrderItemRequest.newBuilder()
                .setId(orderId)
                .build();

        return queryClient.findOrderItemByOrder(request)
                .map(response -> {
                    List<OrderItem> items = new ArrayList<>();
                    if (response != null && response.getDataList() != null) {
                        for (var item : response.getDataList()) {
                            items.add(OrderItem.builder()
                                    .orderItemId((long) item.getId())
                                    .orderId(item.getOrderId())
                                    .productId(item.getProductId())
                                    .quantity(item.getQuantity())
                                    .price(item.getPrice())
                                    .createdAt(parseTimestamp(item.getCreatedAt()))
                                    .updatedAt(parseTimestamp(item.getUpdatedAt()))
                                    .build());
                        }
                    }
                    return items;
                });
    }

    @Override
    public Future<Integer> calculateTotalPrice(Integer orderId) {
        CalculateTotalPriceRequest request = CalculateTotalPriceRequest.newBuilder()
                .setOrderId(orderId)
                .build();

        return commandClient.calculateTotalPrice(request)
                .map(response -> response != null ? response.getTotalPrice() : 0);
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
