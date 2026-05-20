package io.example.transaction.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.transaction.model.OrderItem;
import io.example.transaction.repository.OrderItemRepository;
import io.vertx.core.Future;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.VertxOrderItemQueryServiceGrpcClient;

public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final VertxOrderItemQueryServiceGrpcClient client;

    public OrderItemRepositoryImpl(VertxOrderItemQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<List<OrderItem>> findOrderItemByOrder(Integer orderId) {
        FindByIdOrderItemRequest request = FindByIdOrderItemRequest.newBuilder()
                .setId(orderId)
                .build();

        return client.findOrderItemByOrder(request)
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
                                    .build());
                        }
                    }
                    return items;
                });
    }
}
