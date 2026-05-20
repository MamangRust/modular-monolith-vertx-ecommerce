package io.example.transaction.repository;

import io.vertx.core.Future;
import pb.order.OrderCommon.OrderResponse;

public interface OrderQueryRepository {
    Future<Boolean> findById(Integer orderId);
    Future<OrderResponse> getOrderById(Integer orderId);
}
