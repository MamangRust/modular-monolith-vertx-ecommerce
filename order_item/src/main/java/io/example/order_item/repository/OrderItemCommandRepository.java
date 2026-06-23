package io.example.order_item.repository;

import java.util.List;
import io.example.order_item.domain.requests.CreateOrderItemRecordRequest;
import io.example.order_item.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order_item.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemCommandRepository {
    Future<OrderItem> createOrderItem(CreateOrderItemRecordRequest req);

    Future<OrderItem> updateOrderItem(UpdateOrderItemRecordRequest req);

    Future<List<OrderItem>> trashOrderItem(Long orderId);

    Future<List<OrderItem>> restoreOrderItem(Long orderId);

    Future<Boolean> deleteOrderItemPermanently(Long orderItemId);

    Future<Boolean> deleteOrderItemByOrderPermanent(Long orderId);

    Future<Integer> restoreAllOrderItems();

    Future<Integer> deleteAllPermanentOrderItems();

    Future<Integer> calculateTotalPrice(Long orderId);
}
