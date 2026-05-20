package io.example.order.service;

import io.example.common.model.ApiResponse;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.vertx.core.Future;
import pb.order.OrderCommand.CreateOrderRequest;
import pb.order.OrderCommand.UpdateOrderRequest;

public interface OrderCommandService {
    Future<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest req);
    Future<ApiResponse<OrderResponse>> updateOrder(UpdateOrderRequest req);
    Future<ApiResponse<OrderResponseDeleteAt>> trash(Long id);
    Future<ApiResponse<OrderResponseDeleteAt>> restore(Long id);
    Future<ApiResponse<Boolean>> deletePermanent(Long id);
    Future<ApiResponse<Integer>> restoreAll();
    Future<ApiResponse<Integer>> deleteAllPermanent();
}
