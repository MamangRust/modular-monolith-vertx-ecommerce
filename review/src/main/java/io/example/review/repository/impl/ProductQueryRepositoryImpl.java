package io.example.review.repository.impl;

import io.example.review.repository.ProductQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.VertxProductQueryServiceGrpcClient;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final VertxProductQueryServiceGrpcClient client;

    @Override
    public Future<Boolean> findById(Integer productId) {
        FindByIdProductRequest request = FindByIdProductRequest.newBuilder()
                .setId(productId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }
}
