package io.example.order.repository.impl;

import io.example.order.repository.ProductCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
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
}
