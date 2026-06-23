package io.example.user.repository;

import io.example.user.domain.requests.CreateUserRequest;
import io.example.user.domain.requests.UpdateUserRequest;
import io.example.user.domain.requests.UpdateUserPasswordRequest;
import io.example.user.model.User;
import io.vertx.core.Future;

public interface UserCommandRepository {
  Future<User> createUser(CreateUserRequest request);

  Future<Void> assignDefaultAdminRole(Integer userId);

  Future<User> updateUser(UpdateUserRequest request);

  Future<User> updatePassword(UpdateUserPasswordRequest request);

  Future<User> restore(Long userId);

  Future<User> trashed(Long userId);

  Future<Boolean> deletePermanent(Long userId);

  Future<Integer> restoreAllUsers();

  Future<Integer> deleteAllPermanentUsers();
}
