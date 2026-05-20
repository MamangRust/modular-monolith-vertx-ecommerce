package io.example.role.handler;

import io.example.role.service.RoleQueryService;
import io.vertx.core.Future;
import pb.RoleCommon.*;
import pb.RoleQuery.*;

public class RoleQueryHandler implements pb.VertxRoleQueryServiceGrpcServer.RoleQueryServiceApi {
  private final RoleQueryService service;

  public RoleQueryHandler(RoleQueryService service) {
    this.service = service;
  }

  private pb.Api.PaginationMeta toMeta(io.example.common.model.PaginationMeta meta) {
    if (meta == null) return pb.Api.PaginationMeta.getDefaultInstance();
    return pb.Api.PaginationMeta.newBuilder()
        .setCurrentPage(meta.currentPage())
        .setPageSize(meta.pageSize())
        .setTotalPages(meta.totalPages())
        .setTotalRecords(meta.totalRecords())
        .build();
  }

  @Override
  public Future<ApiResponsePaginationRole> findAllRole(FindAllRoleRequest req) {
    return service.getAllRoles(req)
        .map(resp -> ApiResponsePaginationRole.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromRoleResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponseRole> findByIdRole(FindByIdRoleRequest req) {
    return service.getRoleById(req.getRoleId())
        .map(resp -> {
          var builder = ApiResponseRole.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromRoleResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationRoleDeleteAt> findByActive(FindAllRoleRequest req) {
    return service.getActiveRoles(req)
        .map(resp -> ApiResponsePaginationRoleDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromRoleResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationRoleDeleteAt> findByTrashed(FindAllRoleRequest req) {
    return service.getTrashedRoles(req)
        .map(resp -> ApiResponsePaginationRoleDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromRoleResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsesRole> findByUserId(FindByIdUserRoleRequest req) {
    return service.getRolesByUserId(req.getUserId())
        .map(resp -> {
          var builder = ApiResponsesRole.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.addAllData(resp.data().stream().map(ProtoConverter::fromRoleResponse).toList());
          }
          return builder.build();
        });
  }
}
