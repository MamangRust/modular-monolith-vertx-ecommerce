package io.example.order.repository;

import java.util.List;

import io.example.order.domain.requests.CreateOrderItemRecordRequest;
import io.example.order.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemCommandRepository {
    Future<OrderItem> createOrderItem(CreateOrderItemRecordRequest req);

    Future<OrderItem> updateOrderItem(UpdateOrderItemRecordRequest req);

    Future<List<OrderItem>> trashOrderItem(Long orderId);

    Future<List<OrderItem>> restoreOrderItem(Long orderId);

    Future<Void> deleteOrderItemPermanently(Long orderId);

    Future<Void> restoreAllOrderItems();

    Future<Void> deleteAllPermanentOrderItems();
}
