package io.example.order.repository;

import java.util.List;

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

    /**
     * Semua order yang sedang di-trash, tanpa pagination — dipakai alur
     * restoreAll per order (bukan bulk update).
     */
    Future<List<Order>> findAllTrashed();

    Future<Integer> deleteAllPermanentOrders();
}
