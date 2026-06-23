package io.example.order_item.service;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.order_item.domain.requests.FindAllOrderItemRequest;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.vertx.core.Future;

public interface OrderItemQueryService {
    Future<PagedResult<OrderItemResponse>> getAll(FindAllOrderItemRequest req);

    Future<PagedResult<OrderItemResponseDeleteAt>> getActive(FindAllOrderItemRequest req);

    Future<PagedResult<OrderItemResponseDeleteAt>> getTrashed(FindAllOrderItemRequest req);

    Future<List<OrderItemResponse>> getByOrderId(Long orderId);
}