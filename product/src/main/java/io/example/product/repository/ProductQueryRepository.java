package io.example.product.repository;

import io.example.common.domain.PagedResult;
import io.example.product.domain.requests.FindAllProductCategoryRequest;
import io.example.product.domain.requests.FindAllProductMerchantRequest;
import io.example.product.domain.requests.FindAllProductRequest;
import io.example.product.model.Product;
import io.vertx.core.Future;

public interface ProductQueryRepository {
    Future<PagedResult<Product>> findAll(FindAllProductRequest req);

    Future<PagedResult<Product>> findActive(FindAllProductRequest req);

    Future<PagedResult<Product>> findTrashed(FindAllProductRequest req);

    Future<PagedResult<Product>> findByMerchant(FindAllProductMerchantRequest req);

    Future<PagedResult<Product>> findByCategory(FindAllProductCategoryRequest req);

    Future<Product> findById(Long productId);

    Future<Product> findByIdTrashed(Long productId);

}
