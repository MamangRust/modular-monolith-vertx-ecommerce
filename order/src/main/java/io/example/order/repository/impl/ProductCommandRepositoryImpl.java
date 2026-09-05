package io.example.order.repository.impl;

import io.example.order.repository.ProductCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.product.ProductCommand.DecrementStockRequest;
import pb.product.ProductCommand.IncrementStockRequest;
import pb.product.ProductCommand.UpdateProductCountStockRequest;
import pb.product.VertxProductCommandServiceGrpcClient;

@RequiredArgsConstructor
public class ProductCommandRepositoryImpl implements ProductCommandRepository {
    private final VertxProductCommandServiceGrpcClient client;

    @Override
    public Future<Boolean> updateProductCountStock(Integer productId, Integer stock) {
        UpdateProductCountStockRequest request = UpdateProductCountStockRequest.newBuilder()
                .setProductId(productId)
                .setStock(stock)
                .build();

        return client.updateProductCountStock(request)
                .map(response -> response != null);
    }

    @Override
    public Future<Void> incrementStock(Integer productId, Integer quantity) {
        IncrementStockRequest request = IncrementStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        return client.incrementStock(request).mapEmpty();
    }

    @Override
    public Future<Void> decrementStock(Integer productId, Integer quantity) {
        DecrementStockRequest request = DecrementStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantity(quantity)
                .build();

        return client.decrementStock(request).mapEmpty();
    }
}
