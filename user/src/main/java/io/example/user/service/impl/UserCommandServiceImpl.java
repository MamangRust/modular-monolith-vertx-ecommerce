package io.example.user.service.impl;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.user.domain.requests.CreateUserRequest;
import io.example.user.domain.requests.UpdateUserPasswordRequest;
import io.example.user.domain.requests.UpdateUserRequest;
import io.example.user.model.UserResponse;
import io.example.user.model.UserResponseDeleteAt;
import io.example.user.repository.UserCommandRepository;
import io.example.user.repository.UserQueryRepository;
import io.example.user.service.UserCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {
  private final UserCommandRepository repository;
  private final UserQueryRepository queryRepository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private String hash(String raw) {
    return BCrypt.withDefaults().hashToString(12, raw.toCharArray());
  }

  private Future<Void> evict(Integer id) {
    return redis.delete("user:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<UserResponse> createUser(CreateUserRequest req) {
    var ctx = metrics.startSpan("UserCommandService.createUser",
        Attributes.builder().put("user.email", req.getEmail()).build());

    if (!req.getPassword().equals(req.getConfirmPassword())) {
      return Future.failedFuture(new BadRequestException("Passwords do not match with confirmation"));
    }

    return repository.createUser(req)
        .compose(user -> repository.assignDefaultAdminRole(user.getUserId()).map(user))
        .map(UserResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createUser", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "createUser", e.getMessage()));
  }

  @Override
  public Future<UserResponse> updateUser(UpdateUserRequest req) {
    var ctx = metrics.startSpan("UserCommandService.updateUser",
        Attributes.builder().put("user.id", (long) req.getUserId()).build());
    Integer id = req.getUserId();

    Future<io.example.user.model.User> updateOp;
    if (!req.getPassword().isEmpty()) {
      if (!req.getPassword().equals(req.getConfirmPassword())) {
        return Future.failedFuture(new BadRequestException("Passwords do not match with confirmation"));
      }
      updateOp = repository.updateUser(req)
          .compose(u -> {
            if (u == null)
              return Future.failedFuture(new NotFoundException("User not found"));
            return repository.updatePassword(
                UpdateUserPasswordRequest.builder().password(hash(req.getPassword())).userId(id).build()).map(u);
          });
    } else {
      updateOp = repository.updateUser(req);
    }

    return updateOp
        .compose(u -> {
          if (u == null)
            return Future.failedFuture(new NotFoundException("User not found"));
          return evict(id).map(u);
        })
        .map(UserResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateUser", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateUser", e.getMessage()));
  }

  @Override
  public Future<UserResponseDeleteAt> trashUser(Long req) {
    var ctx = metrics.startSpan("UserCommandService.trashUser",
        Attributes.builder().put("user.id", req).build());

    return repository.trashed(req)
        .compose(r -> {
          if (r == null)
            return Future.failedFuture(new NotFoundException("User not found"));
          return evict(req.intValue()).map(r);
        })
        .map(UserResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashUser", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trashUser", e.getMessage()));
  }

  @Override
  public Future<UserResponseDeleteAt> restoreUser(Long id) {
    var ctx = metrics.startSpan("UserCommandService.restoreUser",
        Attributes.builder().put("user.id", id).build());

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("User not found or must be trashed first"));
          }
          return repository.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("User not found"));
          }
          return evict(id.intValue()).map(v -> r);
        })
        .map(UserResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreUser", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreUser", e.getMessage()));
  }

  @Override
  public Future<Void> deletePermanent(Long req) {

    var ctx = metrics.startSpan("UserService.deletePermanent",
        Attributes.builder().put("user.id", req).build());

    return queryRepository.findByTrashedId(req)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("User not found or must be trashed before permanent deletion"));
          }
          return repository.deletePermanent(req)
              .compose(v -> evict(req.intValue()));
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "User deleted permanently"))
        .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
  }

  @Override
  public Future<Void> restoreAllUsers() {
    var ctx = metrics.startSpan("UserService.restoreAll");

    return repository.restoreAllUsers()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed users found"));
          }
          return redis.delete("user:list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanentUsers() {
    var ctx = metrics.startSpan("UserService.deleteAllPermanent");

    return repository.deleteAllPermanentUsers()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed users found"));
          }
          return redis.delete("user:list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}