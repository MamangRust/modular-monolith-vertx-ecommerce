package io.example.order_item.service;

import java.math.BigDecimal;
import java.util.List;

import io.example.order_item.domain.requests.CreateOrderItemRecordRequest;
import io.example.order_item.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.vertx.core.Future;

public interface OrderItemCommandService {
    Future<OrderItemResponse> create(CreateOrderItemRecordRequest req);

    Future<OrderItemResponse> update(UpdateOrderItemRecordRequest req);

    Future<List<OrderItemResponseDeleteAt>> trash(Long orderId);

    Future<List<OrderItemResponseDeleteAt>> restore(Long orderId);

    Future<Void> deletePermanent(Long orderItemId);

    Future<Void> deleteByOrderPermanent(Long orderId);

    Future<Void> restoreAll();

    Future<Void> deleteAll();

    Future<BigDecimal> calculateTotalPrice(Long orderId);
}