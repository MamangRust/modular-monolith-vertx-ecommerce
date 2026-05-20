package io.example.order.repository;

import io.example.order.model.OrderItem;
import io.vertx.core.Future;
import java.util.List;

public interface OrderItemCommandRepository {
    Future<OrderItem> createOrderItem(Long orderId, Integer productId, Integer quantity, Integer price);
    Future<OrderItem> updateOrderItem(Long orderItemId, Integer quantity, Integer price);
    Future<List<OrderItem>> trashOrderItem(Integer orderId);
    Future<List<OrderItem>> restoreOrderItem(Integer orderId);
    Future<Void> deleteOrderItemPermanently(Integer orderId);
    Future<Void> restoreAllOrderItems();
    Future<Void> deleteAllPermanentOrderItems();
}
