package io.example.product.repository;

import io.example.product.domain.requests.CreateProductRequest;
import io.example.product.model.Product;
import io.example.product.domain.requests.UpdateProductRequest;
import io.vertx.core.Future;

public interface ProductCommandRepository {
    Future<Product> create(CreateProductRequest req);

    Future<Product> update(UpdateProductRequest req);

    Future<Product> updateProductCountStock(Integer productId, Integer countInStock);

    /**
     * Atomically decrement product stock. Returns the updated Product if
     * sufficient stock was available, or fails with
     * InsufficientStockException if count_in_stock < quantity.
     */
    Future<Product> decrementStock(Integer productId, Integer quantity);

    Future<Product> incrementStock(Integer productId, Integer quantity);

    Future<Product> trash(Long productId);

    Future<Product> restore(Long productId);

    Future<Boolean> deletePermanent(Long productId);

    Future<Integer> restoreAll();

    Future<Integer> deleteAll();
}
