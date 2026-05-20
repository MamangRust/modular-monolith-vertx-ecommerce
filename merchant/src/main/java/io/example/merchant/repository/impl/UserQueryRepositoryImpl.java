package io.example.merchant.repository.impl;

import io.example.merchant.repository.UserQueryRepository;
import io.vertx.core.Future;
import pb.user.UserCommon.FindByIdUserRequest;
import pb.user.VertxUserQueryServiceGrpcClient;

public class UserQueryRepositoryImpl implements UserQueryRepository {
  private final VertxUserQueryServiceGrpcClient client;

  public UserQueryRepositoryImpl(VertxUserQueryServiceGrpcClient client) {
    this.client = client;
  }

  @Override
  public Future<pb.user.UserCommon.UserResponse> getUserById(Integer userId) {
    FindByIdUserRequest request = FindByIdUserRequest.newBuilder()
        .setId(userId)
        .build();

    return client.findById(request)
        .map(response -> {
          if (response == null || !response.hasData()) {
            throw new RuntimeException("User not found for ID: " + userId);
          }
          return response.getData();
        });
  }
}
