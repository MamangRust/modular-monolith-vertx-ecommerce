package io.example.product.repository;

import io.example.common.domain.PagedResult;
import io.example.product.model.FindAllProductRequest;
import io.example.product.model.Product;
import io.vertx.core.Future;

public interface ProductQueryRepository {
    Future<PagedResult<Product>> findAll(FindAllProductRequest req);
    Future<PagedResult<Product>> findActive(FindAllProductRequest req);
    Future<PagedResult<Product>> findTrashed(FindAllProductRequest req);
    Future<PagedResult<Product>> findByMerchant(Long merchantId, String search, Long categoryId, Integer minPrice, Integer maxPrice, int page, int pageSize);
    Future<PagedResult<Product>> findByCategory(String categoryName, String search, Integer minPrice, Integer maxPrice, int page, int pageSize);
    Future<Product> findById(Long productId);
    Future<Product> findByIdTrashed(Long productId);
}
