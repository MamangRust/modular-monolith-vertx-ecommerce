package io.example.user.repository;

import io.example.common.domain.PagedResult;
import io.example.user.domain.requests.FindAllUsers;
import io.example.user.model.User;
import io.vertx.core.Future;

public interface UserQueryRepository {
  Future<PagedResult<User>> getUsers(FindAllUsers req);

  Future<PagedResult<User>> getActiveUsers(FindAllUsers req);

  Future<PagedResult<User>> getTrashedUsers(FindAllUsers req);

  Future<User> getUserById(Long userId);

  Future<User> getUserByEmail(String email);

  Future<User> findByTrashedId(Long userId);
}
