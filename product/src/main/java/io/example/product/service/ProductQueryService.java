package io.example.product.service;

import io.example.common.domain.PagedResult;
import io.example.product.domain.requests.FindAllProductCategoryRequest;
import io.example.product.domain.requests.FindAllProductMerchantRequest;
import io.example.product.domain.requests.FindAllProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.vertx.core.Future;

public interface ProductQueryService {
    Future<PagedResult<ProductResponse>> getAll(FindAllProductRequest req);

    Future<PagedResult<ProductResponseDeleteAt>> getActive(FindAllProductRequest req);

    Future<PagedResult<ProductResponseDeleteAt>> getTrashed(FindAllProductRequest req);

    Future<PagedResult<ProductResponse>> getByMerchant(FindAllProductMerchantRequest req);

    Future<PagedResult<ProductResponse>> getByCategoryName(FindAllProductCategoryRequest req);

    Future<ProductResponse> getById(Long id);
}