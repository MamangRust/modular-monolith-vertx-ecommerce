package io.example.order.repository;

import io.example.order.domain.requests.CreateOrderRecordRequest;
import io.example.order.domain.requests.UpdateOrderRecordRequest;
import io.example.order.model.Order;
import io.vertx.core.Future;

public interface OrderCommandRepository {
    Future<Order> createOrder(CreateOrderRecordRequest req);

    Future<Order> updateOrder(UpdateOrderRecordRequest req);

    Future<Order> trashOrder(Long orderId);

    Future<Order> restoreOrder(Long orderId);

    Future<Boolean> deleteOrderPermanently(Long orderId);

    Future<Integer> restoreAllOrders();

    Future<Integer> deleteAllPermanentOrders();
}
