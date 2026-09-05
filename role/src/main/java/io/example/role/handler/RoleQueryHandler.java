package io.example.role.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.common.grpc.GrpcServerBinder;
import io.example.role.domain.requests.FindAllRolesRequest;
import io.example.role.service.RoleQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.RoleCommon.ApiResponsePaginationRole;
import pb.RoleCommon.ApiResponsePaginationRoleDeleteAt;
import pb.RoleCommon.ApiResponseRole;
import pb.RoleCommon.ApiResponsesRole;
import pb.RoleCommon.FindByIdRoleRequest;
import pb.RoleQuery.FindAllRoleRequest;
import pb.RoleQuery.FindByIdUserRoleRequest;

@RequiredArgsConstructor
public class RoleQueryHandler implements pb.VertxRoleQueryServiceGrpcServer.RoleQueryServiceApi {
  private final RoleQueryService service;

  private FindAllRolesRequest toDomainReq(FindAllRoleRequest req) {
    return FindAllRolesRequest.builder()
        .search(req.getSearch())
        .page(req.getPage() > 0 ? req.getPage() : 1)
        .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
        .build();
  }

  private pb.Api.PaginationMeta toMeta(int totalRecords, int page, int pageSize) {
    int currentPage = page > 0 ? page : 1;
    int size = pageSize > 0 ? pageSize : 10;
    int totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
    return pb.Api.PaginationMeta.newBuilder()
        .setCurrentPage(currentPage)
        .setPageSize(size)
        .setTotalPages(totalPages)
        .setTotalRecords(totalRecords)
        .build();
  }

  @Override
  public Future<ApiResponsePaginationRole> findAllRole(FindAllRoleRequest req) {
    FindAllRolesRequest domainReq = toDomainReq(req);
    return service.getAllRoles(domainReq)
        .map(res -> ApiResponsePaginationRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromRoleResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponseRole> findByIdRole(FindByIdRoleRequest req) {
    return service.getRoleById((long) req.getRoleId())
        .map(res -> ApiResponseRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromRoleResponse(res))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponsePaginationRoleDeleteAt> findByActive(FindAllRoleRequest req) {
    FindAllRolesRequest domainReq = toDomainReq(req);
    return service.getActiveRoles(domainReq)
        .map(res -> ApiResponsePaginationRoleDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromRoleResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponsePaginationRoleDeleteAt> findByTrashed(FindAllRoleRequest req) {
    FindAllRolesRequest domainReq = toDomainReq(req);
    return service.getTrashedRoles(domainReq)
        .map(res -> ApiResponsePaginationRoleDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromRoleResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponsesRole> findByUserId(FindByIdUserRoleRequest req) {
    return service.getRolesByUserId((long) req.getUserId())
        .map(res -> ApiResponsesRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.stream().map(ProtoConverter::fromRoleResponse).toList())
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public pb.VertxRoleQueryServiceGrpcServer.RoleQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.VertxRoleQueryServiceGrpcServer.FindAllRole, this::findAllRole);
    GrpcServerBinder.bind(server, pb.VertxRoleQueryServiceGrpcServer.FindByIdRole, this::findByIdRole);
    GrpcServerBinder.bind(server, pb.VertxRoleQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.VertxRoleQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    GrpcServerBinder.bind(server, pb.VertxRoleQueryServiceGrpcServer.FindByUserId, this::findByUserId);
    return this;
  }
}