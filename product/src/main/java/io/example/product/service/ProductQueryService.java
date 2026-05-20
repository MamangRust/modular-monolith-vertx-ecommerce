package io.example.product.service;

import io.example.common.domain.ApiResponse;
import io.example.common.domain.ApiResponsePagination;
import io.example.product.model.FindAllProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.vertx.core.Future;
import java.util.List;

public interface ProductQueryService {
    Future<ApiResponsePagination<List<ProductResponse>>> getAll(FindAllProductRequest req);
    Future<ApiResponsePagination<List<ProductResponse>>> getActive(FindAllProductRequest req);
    Future<ApiResponsePagination<List<ProductResponseDeleteAt>>> getTrashed(FindAllProductRequest req);
    Future<ApiResponsePagination<List<ProductResponse>>> getByMerchant(Long merchantId, String search, Long categoryId, Integer minPrice, Integer maxPrice, int page, int pageSize);
    Future<ApiResponsePagination<List<ProductResponse>>> getByCategoryName(String categoryName, String search, Integer minPrice, Integer maxPrice, int page, int pageSize);
    Future<ApiResponse<ProductResponse>> getById(Long id);
}
