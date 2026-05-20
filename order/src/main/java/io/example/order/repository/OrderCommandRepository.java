package io.example.order.repository;

import io.example.order.model.CreateOrderRecord;
import io.example.order.model.UpdateOrderRecord;
import io.example.order.model.Order;
import io.vertx.core.Future;

public interface OrderCommandRepository {
    Future<Order> createOrder(CreateOrderRecord req);
    Future<Order> updateOrder(UpdateOrderRecord req);
    Future<Order> trashOrder(Long orderId);
    Future<Order> restoreOrder(Long orderId);
    Future<Void> deleteOrderPermanently(Long orderId);
    Future<Integer> restoreAllOrders();
    Future<Integer> deleteAllPermanentOrders();
}
