package io.example.cart.repository;

import io.example.cart.model.ProductInfo;
import io.vertx.core.Future;

public interface ProductQueryRepository {
    Future<ProductInfo> findById(Integer productId);
}
