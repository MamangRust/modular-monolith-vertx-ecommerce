package io.example.order.repository;

import io.example.order.domain.requests.CreateOrderItemRecordRequest;
import io.example.order.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemCommandRepository {
    Future<OrderItem> createOrderItem(CreateOrderItemRecordRequest req);

    Future<OrderItem> updateOrderItem(UpdateOrderItemRecordRequest req);

    Future<Void> deleteOrderItemPermanently(Long orderId);

    Future<Void> deleteOrderItemByIdPermanently(Long orderItemId);

    Future<Void> deleteAllPermanentOrderItems();
}
