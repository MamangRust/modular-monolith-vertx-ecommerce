package io.example.product.repository;

import io.example.product.domain.requests.CreateProductRequest;
import io.example.product.model.Product;
import io.example.product.domain.requests.UpdateProductRequest;
import io.vertx.core.Future;

public interface ProductCommandRepository {
    Future<Product> create(CreateProductRequest req);

    Future<Product> update(UpdateProductRequest req);

    Future<Product> updateProductCountStock(Integer productId, Integer countInStock);

    Future<Product> trash(Long productId);

    Future<Product> restore(Long productId);

    Future<Boolean> deletePermanent(Long productId);

    Future<Integer> restoreAll();

    Future<Integer> deleteAll();
}
