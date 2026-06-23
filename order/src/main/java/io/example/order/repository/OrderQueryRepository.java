package io.example.order.repository;

import io.example.common.domain.PagedResult;
import io.example.order.domain.requests.FindAllOrderByMerchantRequest;
import io.example.order.domain.requests.FindAllOrderRequest;
import io.example.order.model.Order;
import io.vertx.core.Future;

public interface OrderQueryRepository {
    Future<PagedResult<Order>> getOrders(FindAllOrderRequest req);

    Future<PagedResult<Order>> getOrdersActive(FindAllOrderRequest req);

    Future<PagedResult<Order>> getOrdersTrashed(FindAllOrderRequest req);

    Future<PagedResult<Order>> getOrdersByMerchant(FindAllOrderByMerchantRequest req);

    Future<Order> getOrderById(Long orderId);

    Future<Order> findByTrashedId(Long orderId);
}
