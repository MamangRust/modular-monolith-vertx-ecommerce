package io.example.review.repository.impl;

import io.example.review.repository.UserQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.user.UserCommon.FindByIdUserRequest;
import pb.user.VertxUserQueryServiceGrpcClient;

@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {
    private final VertxUserQueryServiceGrpcClient client;

    @Override
    public Future<Boolean> findById(Integer userId) {
        FindByIdUserRequest request = FindByIdUserRequest.newBuilder()
                .setId(userId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }
}
