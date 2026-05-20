package io.example.transaction.repository;

import io.vertx.core.Future;
import pb.user.UserCommon.UserResponse;

public interface UserQueryRepository {
    Future<UserResponse> getUserById(Integer userId);
}
