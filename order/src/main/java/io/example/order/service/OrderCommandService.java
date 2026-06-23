package io.example.order.service;

import io.example.order.domain.requests.CreateOrderRequest;
import io.example.order.domain.requests.UpdateOrderRequest;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.vertx.core.Future;

public interface OrderCommandService {
    Future<OrderResponse> createOrder(CreateOrderRequest req);

    Future<OrderResponse> updateOrder(UpdateOrderRequest req);

    Future<OrderResponseDeleteAt> trash(Long id);

    Future<OrderResponseDeleteAt> restore(Long id);

    Future<Void> deletePermanent(Long id);

    Future<Void> restoreAll();

    Future<Void> deleteAllPermanent();
}