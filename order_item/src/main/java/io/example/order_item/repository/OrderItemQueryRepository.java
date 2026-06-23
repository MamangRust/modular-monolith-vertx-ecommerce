package io.example.order_item.repository;

import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.order_item.domain.requests.FindAllOrderItemRequest;
import io.example.order_item.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemQueryRepository {
    Future<PagedResult<OrderItem>> getOrderItems(FindAllOrderItemRequest req);

    Future<PagedResult<OrderItem>> getOrderItemsActive(FindAllOrderItemRequest req);

    Future<PagedResult<OrderItem>> getOrderItemsTrashed(FindAllOrderItemRequest req);

    Future<OrderItem> findByTrashedId(Long orderItemId);

    Future<List<OrderItem>> getOrderItemsByOrder(Long orderId);
}
