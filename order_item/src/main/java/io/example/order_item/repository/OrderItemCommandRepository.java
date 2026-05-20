package io.example.order_item.repository;

import java.util.List;
import io.example.order_item.model.OrderItem;
import io.vertx.core.Future;

public interface OrderItemCommandRepository {
    Future<OrderItem> createOrderItem(Integer orderId, Integer productId, Integer quantity, Integer price);
    Future<OrderItem> updateOrderItem(Integer orderItemId, Integer quantity, Integer price);
    Future<List<OrderItem>> trashOrderItem(Integer orderId);
    Future<List<OrderItem>> restoreOrderItem(Integer orderId);
    Future<Void> deleteOrderItemPermanently(Integer orderItemId);
    Future<Void> deleteOrderItemByOrderPermanent(Integer orderId);
    Future<Void> restoreAllOrderItems();
    Future<Void> deleteAllPermanentOrderItems();
    Future<Integer> calculateTotalPrice(Integer orderId);
}
