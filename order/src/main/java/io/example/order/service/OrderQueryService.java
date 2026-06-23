package io.example.order.service;

import io.example.common.domain.PagedResult;
import io.example.order.domain.requests.FindAllOrderByMerchantRequest;
import io.example.order.domain.requests.FindAllOrderRequest;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.vertx.core.Future;

public interface OrderQueryService {
    Future<PagedResult<OrderResponse>> getAll(FindAllOrderRequest req);

    Future<PagedResult<OrderResponse>> getActive(FindAllOrderRequest req);

    Future<PagedResult<OrderResponseDeleteAt>> getTrashed(FindAllOrderRequest req);

    Future<PagedResult<OrderResponse>> getByMerchant(FindAllOrderByMerchantRequest req);

    Future<OrderResponse> getById(Long id);
}