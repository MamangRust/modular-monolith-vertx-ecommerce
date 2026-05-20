package io.example.product.repository.impl;

import io.example.product.repository.CategoryQueryRepository;
import io.vertx.core.Future;
import pb.category.CategoryCommon.FindByIdCategoryRequest;
import pb.category.VertxCategoryQueryServiceGrpcClient;

public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {
    private final pb.category.VertxCategoryQueryServiceGrpcClient client;

    public CategoryQueryRepositoryImpl(pb.category.VertxCategoryQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> findById(Integer categoryId) {
        FindByIdCategoryRequest request = FindByIdCategoryRequest.newBuilder()
                .setId(categoryId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData())
                .recover(err -> Future.succeededFuture(false));
    }
}
