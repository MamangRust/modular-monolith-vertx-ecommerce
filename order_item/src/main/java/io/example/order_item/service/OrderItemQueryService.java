package io.example.order_item.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.vertx.core.Future;
import pb.order_item.OrderItemQuery.FindAllOrderItemRequest;

public interface OrderItemQueryService {
    Future<ApiResponsePagination<List<OrderItemResponse>>> getAll(FindAllOrderItemRequest req);
    Future<ApiResponsePagination<List<OrderItemResponseDeleteAt>>> getActive(FindAllOrderItemRequest req);
    Future<ApiResponsePagination<List<OrderItemResponseDeleteAt>>> getTrashed(FindAllOrderItemRequest req);
    Future<ApiResponse<List<OrderItemResponse>>> getByOrderId(Integer orderId);
}
