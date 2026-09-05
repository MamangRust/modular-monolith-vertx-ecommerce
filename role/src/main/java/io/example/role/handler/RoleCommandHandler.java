package io.example.role.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.common.grpc.GrpcServerBinder;
import io.example.role.service.RoleCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.RoleCommand.CreateRoleRequest;
import pb.RoleCommand.UpdateRoleRequest;
import pb.RoleCommon.ApiResponseRole;
import pb.RoleCommon.ApiResponseRoleAll;
import pb.RoleCommon.ApiResponseRoleDelete;
import pb.RoleCommon.FindByIdRoleRequest;

@RequiredArgsConstructor
public class RoleCommandHandler implements pb.VertxRoleCommandServiceGrpcServer.RoleCommandServiceApi {
  private final RoleCommandService service;

  @Override
  public Future<ApiResponseRole> createRole(CreateRoleRequest req) {
    var reqDomain = io.example.role.domain.requests.CreateRoleRequest.builder()
        .name(req.getName())
        .build();

    return service.createRole(reqDomain)
        .map(data -> ApiResponseRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromRoleResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRole> updateRole(UpdateRoleRequest req) {
    var reqDomain = io.example.role.domain.requests.UpdateRoleRequest.builder()
        .roleId(req.getId())
        .name(req.getName())
        .build();

    return service.updateRole(reqDomain)
        .map(data -> ApiResponseRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromRoleResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRole> trashedRole(FindByIdRoleRequest req) {
    return service.trashRole((long) req.getRoleId())
        .map(data -> ApiResponseRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromRoleResponseDeleteAtToResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRole> restoreRole(FindByIdRoleRequest req) {
    return service.restoreRole((long) req.getRoleId())
        .map(data -> ApiResponseRole.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromRoleResponseDeleteAtToResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRoleDelete> deleteRolePermanent(FindByIdRoleRequest req) {
    return service.deletePermanent((long) req.getRoleId())
        .map(v -> ApiResponseRoleDelete.newBuilder()
            .setStatus("success")
            .setMessage("Role deleted permanently")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRoleAll> restoreAllRole(Empty req) {
    return service.restoreAllRoles()
        .map(v -> ApiResponseRoleAll.newBuilder()
            .setStatus("success")
            .setMessage("All roles restored successfully")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseRoleAll> deleteAllRolePermanent(Empty req) {
    return service.deleteAllPermanentRoles()
        .map(v -> ApiResponseRoleAll.newBuilder()
            .setStatus("success")
            .setMessage("All roles permanently deleted")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.VertxRoleCommandServiceGrpcServer.RoleCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.CreateRole, this::createRole);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.UpdateRole, this::updateRole);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.TrashedRole, this::trashedRole);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.RestoreRole, this::restoreRole);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.DeleteRolePermanent, this::deleteRolePermanent);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.RestoreAllRole, this::restoreAllRole);
    GrpcServerBinder.bind(server, pb.VertxRoleCommandServiceGrpcServer.DeleteAllRolePermanent, this::deleteAllRolePermanent);
    return this;
  }
}