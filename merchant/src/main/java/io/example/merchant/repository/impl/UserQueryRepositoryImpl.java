package io.example.merchant.repository.impl;

import io.example.merchant.repository.UserQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.user.UserCommon.FindByIdUserRequest;
import pb.user.VertxUserQueryServiceGrpcClient;

@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {
  private final VertxUserQueryServiceGrpcClient client;

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
