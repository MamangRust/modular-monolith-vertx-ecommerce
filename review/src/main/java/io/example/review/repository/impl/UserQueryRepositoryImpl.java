package io.example.review.repository.impl;

import io.example.review.repository.UserQueryRepository;
import io.vertx.core.Future;
import pb.user.UserCommon.FindByIdUserRequest;
import pb.user.VertxUserQueryServiceGrpcClient;

public class UserQueryRepositoryImpl implements UserQueryRepository {
    private final VertxUserQueryServiceGrpcClient client;

    public UserQueryRepositoryImpl(VertxUserQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> findById(Integer userId) {
        FindByIdUserRequest request = FindByIdUserRequest.newBuilder()
                .setId(userId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }
}
