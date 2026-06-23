package io.example.product.service;

import io.example.product.domain.requests.CreateProductRequest;
import io.example.product.domain.requests.UpdateProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.vertx.core.Future;

public interface ProductCommandService {
    Future<ProductResponse> create(CreateProductRequest req);

    Future<ProductResponse> update(UpdateProductRequest req);

    Future<ProductResponse> updateProductCountStock(Integer productId, Integer countInStock);

    Future<ProductResponseDeleteAt> trash(Long id);

    Future<ProductResponseDeleteAt> restore(Long id);

    Future<Void> deletePermanent(Long id);

    Future<Void> restoreAll();

    Future<Void> deleteAllPermanent();
}