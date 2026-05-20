package io.example.order.service;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.vertx.core.Future;
import java.util.List;

public interface OrderQueryService {
    Future<ApiResponsePagination<List<OrderResponse>>> getAll(String search, int page, int pageSize);
    Future<ApiResponsePagination<List<OrderResponse>>> getActive(String search, int page, int pageSize);
    Future<ApiResponsePagination<List<OrderResponseDeleteAt>>> getTrashed(String search, int page, int pageSize);
    Future<ApiResponsePagination<List<OrderResponse>>> getByMerchant(Integer merchantId, String search, int page, int pageSize);
    Future<ApiResponse<OrderResponse>> getById(Long id);
}
