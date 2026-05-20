package io.example.product.repository;

import io.example.product.model.CreateProductRequest;
import io.example.product.model.Product;
import io.example.product.model.UpdateProductRequest;
import io.vertx.core.Future;

public interface ProductCommandRepository {
    Future<Product> create(CreateProductRequest req);
    Future<Product> update(UpdateProductRequest req);
    Future<Product> updateProductCountStock(Long productId, Integer countInStock);
    Future<Product> trash(Long productId);
    Future<Product> restore(Long productId);
    Future<Void> deletePermanent(Long productId);
    Future<Void> restoreAll();
    Future<Void> deleteAll();
}
