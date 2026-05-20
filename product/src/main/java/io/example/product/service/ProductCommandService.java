package io.example.product.service;

import io.example.common.domain.ApiResponse;
import io.example.product.model.CreateProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.example.product.model.UpdateProductRequest;
import io.vertx.core.Future;

public interface ProductCommandService {
    Future<ApiResponse<ProductResponse>> create(CreateProductRequest req);
    Future<ApiResponse<ProductResponse>> update(UpdateProductRequest req);
    Future<ApiResponse<ProductResponse>> updateProductCountStock(Long productId, Integer countInStock);
    Future<ApiResponse<ProductResponseDeleteAt>> trash(Long id);
    Future<ApiResponse<ProductResponseDeleteAt>> restore(Long id);
    Future<ApiResponse<Void>> deletePermanent(Long id);
    Future<ApiResponse<Void>> restoreAll();
    Future<ApiResponse<Void>> deleteAllPermanent();
}
