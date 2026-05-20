package io.example.transaction.repository;

import java.util.List;
import io.example.transaction.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemRepository {
    Future<List<OrderItem>> findOrderItemByOrder(Integer orderId);
}
