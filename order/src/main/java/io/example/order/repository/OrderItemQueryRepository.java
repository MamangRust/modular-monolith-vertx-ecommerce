package io.example.order.repository;

import io.example.order.model.OrderItem;
import io.vertx.core.Future;
import java.util.List;

public interface OrderItemQueryRepository {
    Future<List<OrderItem>> getOrderItemsByOrder(Integer orderId);
    Future<Integer> calculateTotalPrice(Integer orderId);
}
