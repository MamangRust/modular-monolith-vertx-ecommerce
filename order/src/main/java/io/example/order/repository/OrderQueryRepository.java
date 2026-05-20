package io.example.order.repository;

import io.example.common.domain.PagedResult;
import io.example.order.model.Order;
import io.vertx.core.Future;

public interface OrderQueryRepository {
    Future<PagedResult<Order>> getOrders(String search, int page, int pageSize);
    Future<PagedResult<Order>> getOrdersActive(String search, int page, int pageSize);
    Future<PagedResult<Order>> getOrdersTrashed(String search, int page, int pageSize);
    Future<PagedResult<Order>> getOrdersByMerchant(Integer merchantId, String search, int page, int pageSize);
    Future<Order> getOrderById(Long orderId);
}
