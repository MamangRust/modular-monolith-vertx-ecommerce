package io.example.transaction.repository.impl;

import io.example.transaction.repository.OrderQueryRepository;
import io.vertx.core.Future;
import pb.order.OrderCommon.FindByIdOrderRequest;
import pb.order.VertxOrderQueryServiceGrpcClient;

public class OrderQueryRepositoryImpl implements OrderQueryRepository {
    private final VertxOrderQueryServiceGrpcClient client;

    public OrderQueryRepositoryImpl(VertxOrderQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> findById(Integer orderId) {
        FindByIdOrderRequest request = FindByIdOrderRequest.newBuilder()
                .setId(orderId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }

    @Override
    public Future<pb.order.OrderCommon.OrderResponse> getOrderById(Integer orderId) {
        FindByIdOrderRequest request = FindByIdOrderRequest.newBuilder()
                .setId(orderId)
                .build();

        return client.findById(request)
                .map(response -> {
                    if (response == null || !response.hasData()) {
                        throw new RuntimeException("Order not found: " + orderId);
                    }
                    return response.getData();
                });
    }
}
