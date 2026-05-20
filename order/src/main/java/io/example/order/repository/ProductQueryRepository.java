package io.example.order.repository;

import io.example.order.model.ProductInfo;
import io.vertx.core.Future;

public interface ProductQueryRepository {
    Future<ProductInfo> findById(Integer productId);
}
