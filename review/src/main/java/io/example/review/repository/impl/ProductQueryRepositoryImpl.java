package io.example.review.repository.impl;

import io.example.review.repository.ProductQueryRepository;
import io.vertx.core.Future;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.VertxProductQueryServiceGrpcClient;

public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final VertxProductQueryServiceGrpcClient client;

    public ProductQueryRepositoryImpl(VertxProductQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> findById(Integer productId) {
        FindByIdProductRequest request = FindByIdProductRequest.newBuilder()
                .setId(productId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }
}
