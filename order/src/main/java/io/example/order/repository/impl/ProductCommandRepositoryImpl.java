package io.example.order.repository.impl;

import io.example.order.repository.ProductCommandRepository;
import io.vertx.core.Future;
import pb.product.ProductCommand.UpdateProductCountStockRequest;
import pb.product.VertxProductCommandServiceGrpcClient;

public class ProductCommandRepositoryImpl implements ProductCommandRepository {
    private final VertxProductCommandServiceGrpcClient client;

    public ProductCommandRepositoryImpl(VertxProductCommandServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> updateProductCountStock(Integer productId, Integer stock) {
        UpdateProductCountStockRequest request = UpdateProductCountStockRequest.newBuilder()
                .setProductId(productId)
                .setStock(stock)
                .build();

        return client.updateProductCountStock(request)
                .map(response -> response != null);
    }
}
