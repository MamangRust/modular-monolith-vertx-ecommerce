package io.example.order_item.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.vertx.core.Future;
import pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest;
import pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest;

public interface OrderItemCommandService {
    Future<ApiResponse<OrderItemResponse>> create(CreateOrderItemRecordRequest req);
    Future<ApiResponse<OrderItemResponse>> update(UpdateOrderItemRecordRequest req);
    Future<ApiResponse<List<OrderItemResponseDeleteAt>>> trash(Integer orderId);
    Future<ApiResponse<List<OrderItemResponseDeleteAt>>> restore(Integer orderId);
    Future<ApiResponse<Void>> deletePermanent(Integer orderItemId);
    Future<ApiResponse<Void>> deleteByOrderPermanent(Integer orderId);
    Future<ApiResponse<Void>> restoreAll();
    Future<ApiResponse<Void>> deleteAll();
    Future<ApiResponse<Integer>> calculateTotalPrice(Integer orderId);
}
