package io.example.role.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.role.domain.requests.FindAllRolesRequest;
import io.example.role.model.Role;
import io.example.role.model.RoleResponse;
import io.example.role.model.RoleResponseDeleteAt;
import io.example.role.repository.RoleQueryRepository;
import io.example.role.service.RoleQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleQueryServiceImpl implements RoleQueryService {
  private static final Logger log = LoggerFactory.getLogger(RoleQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final RoleQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "role:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private PagedResult<RoleResponse> mapPagination(PagedResult<Role> res) {
    List<RoleResponse> data = res.getData().stream().map(RoleResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<RoleResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Role> res) {
    List<RoleResponseDeleteAt> data = res.getData().stream().map(RoleResponseDeleteAt::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  @Override
  public Future<PagedResult<RoleResponse>> getAllRoles(FindAllRolesRequest req) {
    var ctx = metrics.startSpan("RoleQueryService.getAllRoles");
    String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Role> typedCached = mapper.readValue(jsonStr, new TypeReference<PagedResult<Role>>() {
              });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached roles: {}", e.getMessage());
            }
          }
          return repository.getRoles(FindAllRolesRequest.builder()
              .search(req.getSearch())
              .page(req.getPage())
              .pageSize(req.getPageSize())
              .build())
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAllRoles", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getAllRoles", e.getMessage()));
  }

  @Override
  public Future<PagedResult<RoleResponseDeleteAt>> getActiveRoles(FindAllRolesRequest req) {
    var ctx = metrics.startSpan("RoleQueryService.getActiveRoles");
    String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Role> typedCached = mapper.readValue(jsonStr, new TypeReference<PagedResult<Role>>() {
              });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active roles: {}", e.getMessage());
            }
          }
          return repository.getActiveRoles(FindAllRolesRequest.builder()
              .search(req.getSearch())
              .page(req.getPage())
              .pageSize(req.getPageSize())
              .build())
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveRoles", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getActiveRoles", e.getMessage()));
  }

  @Override
  public Future<PagedResult<RoleResponseDeleteAt>> getTrashedRoles(FindAllRolesRequest req) {
    var ctx = metrics.startSpan("RoleQueryService.getTrashedRoles");
    String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Role> typedCached = mapper.readValue(jsonStr, new TypeReference<PagedResult<Role>>() {
              });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed roles: {}", e.getMessage());
            }
          }
          return repository.getTrashedRoles(FindAllRolesRequest.builder()
              .search(req.getSearch())
              .page(req.getPage())
              .pageSize(req.getPageSize())
              .build())
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedRoles", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedRoles", e.getMessage()));
  }

  @Override
  public Future<RoleResponse> getRoleById(Long roleId) {
    var ctx = metrics.startSpan("RoleQueryService.getRoleById",
        Attributes.builder().put("role.id", (long) roleId).build());
    String key = CACHE_PREFIX + roleId;

    return redis.getJson(key, Role.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(RoleResponse.from(cached));
          }
          return repository.getRoleById(roleId)
              .compose(db -> {
                if (db == null) {
                  return Future.<Role>failedFuture(new NotFoundException("Role not found"));
                }
                return redis.setJson(key, db, CACHE_TTL).<Role>map(v -> db);
              })
              .map(RoleResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getRoleById", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getRoleById", e.getMessage()));
  }

  @Override
  public Future<List<RoleResponse>> getRolesByUserId(Long userId) {
    var ctx = metrics.startSpan("RoleQueryService.getRolesByUserId",
        Attributes.builder().put("user.id", (long) userId).build());
    String cacheKey = CACHE_PREFIX + "user:" + userId;

    return redis.getJsonList(cacheKey, Role.class)
        .compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            return Future.succeededFuture(cached.stream().map(RoleResponse::from).toList());
          }
          return repository.getRolesByUserId(userId)
              .compose(roles -> {
                if (roles == null || roles.isEmpty()) {
                  return Future.succeededFuture(List.<Role>of());
                }
                return redis.setJsonList(cacheKey, roles, CACHE_TTL).<List<Role>>map(v -> roles);
              })
              .map(roles -> roles.stream().map(RoleResponse::from).toList());
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getRolesByUserId", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getRolesByUserId", e.getMessage()));
  }
}