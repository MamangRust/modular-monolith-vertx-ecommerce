package io.example.role.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.role.domain.requests.CreateRoleRequest;
import io.example.role.domain.requests.UpdateRoleRequest;
import io.example.role.model.RoleResponse;
import io.example.role.model.RoleResponseDeleteAt;
import io.example.role.repository.RoleCommandRepository;
import io.example.role.repository.RoleQueryRepository;
import io.example.role.service.RoleCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleCommandServiceImpl implements RoleCommandService {
  private final RoleCommandRepository repository;
  private final RoleQueryRepository queryRepository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private Future<Void> evict(Long id) {
    return redis.delete("role:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<RoleResponse> createRole(CreateRoleRequest req) {
    var ctx = metrics.startSpan("RoleCommandService.createRole",
        Attributes.builder().put("role.name", req.getName()).build());

    return repository.createRole(req)
        .map(RoleResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createRole", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "createRole", e.getMessage()));
  }

  @Override
  public Future<RoleResponse> updateRole(UpdateRoleRequest req) {
    var ctx = metrics.startSpan("RoleCommandService.updateRole",
        Attributes.builder().put("role.id", (long) req.getRoleId()).build());
    Long id = (long) req.getRoleId();

    return repository.updateRole(req)
        .compose(role -> {
          if (role == null) {
            return Future.failedFuture(new NotFoundException("Role not found"));
          }
          return evict(id).map(role);
        })
        .map(RoleResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateRole", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateRole", e.getMessage()));
  }

  @Override
  public Future<RoleResponseDeleteAt> trashRole(Long roleId) {
    var ctx = metrics.startSpan("RoleCommandService.trashRole",
        Attributes.builder().put("role.id", (long) roleId).build());

    return repository.trashed(roleId)
        .compose(role -> {
          if (role == null) {
            return Future.failedFuture(new NotFoundException("Role not found"));
          }
          return evict(roleId).map(role);
        })
        .map(RoleResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashRole", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trashRole", e.getMessage()));
  }

  @Override
  public Future<RoleResponseDeleteAt> restoreRole(Long roleId) {
    var ctx = metrics.startSpan("RoleCommandService.restoreRole",
        Attributes.builder().put("role.id", (long) roleId).build());

    return queryRepository.findByTrashedId(roleId)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Role not found or must be trashed first"));
          }
          return repository.restore(roleId);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Role not found"));
          }
          return evict(roleId).map(v -> r);
        })
        .map(RoleResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreRole", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreRole", e.getMessage()));
  }

  @Override
  public Future<Void> deletePermanent(Long roleId) {
    var ctx = metrics.startSpan("RoleCommandService.deletePermanent",
        Attributes.builder().put("role.id", (long) roleId).build());

    return queryRepository.findByTrashedId(roleId)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("Role not found or must be trashed before permanent deletion"));
          }
          return repository.deletePermanent(roleId)
              .compose(v -> evict(roleId));
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Role deleted permanently"))
        .onFailure(e -> metrics.completeSpanError(ctx, "deletePermanent", e.getMessage()));
  }

  @Override
  public Future<Void> restoreAllRoles() {
    var ctx = metrics.startSpan("RoleService.restoreAll");

    return repository.restoreAllRoles()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed roles found"));
          }
          return redis.delete("role:list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanentRoles() {
    var ctx = metrics.startSpan("RoleService.deleteAllPermanent");

    return repository.deleteAllPermanentRoles()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed roles found"));
          }
          return redis.delete("role:list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}