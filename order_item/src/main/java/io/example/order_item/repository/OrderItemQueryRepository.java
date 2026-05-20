package io.example.order_item.repository;

import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.order_item.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemQueryRepository {
    Future<PagedResult<OrderItem>> getOrderItems(String search, int page, int pageSize);
    Future<PagedResult<OrderItem>> getOrderItemsActive(String search, int page, int pageSize);
    Future<PagedResult<OrderItem>> getOrderItemsTrashed(String search, int page, int pageSize);
    Future<List<OrderItem>> getOrderItemsByOrder(Integer orderId);
}
