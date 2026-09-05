package io.example.user.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.user.service.UserCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.user.UserCommand.CreateUserRequest;
import pb.user.UserCommand.UpdateUserRequest;
import pb.user.UserCommon.ApiResponseUser;
import pb.user.UserCommon.ApiResponseUserAll;
import pb.user.UserCommon.ApiResponseUserDelete;
import pb.user.UserCommon.ApiResponseUserDeleteAt;
import pb.user.UserCommon.FindByIdUserRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class UserCommandHandler implements pb.user.VertxUserCommandServiceGrpcServer.UserCommandServiceApi {
    private final UserCommandService service;

    @Override
    public Future<ApiResponseUser> create(CreateUserRequest req) {
        var reqDomain = io.example.user.domain.requests.CreateUserRequest.builder()
                .firstName(req.getFirstname())
                .lastName(req.getLastname())
                .email(req.getEmail())
                .password(req.getPassword())
                .confirmPassword(req.getConfirmPassword())
                .build();

        return service.createUser(reqDomain)
                .map(data -> ApiResponseUser.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toUserResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUser> update(UpdateUserRequest req) {
        var reqDomain = io.example.user.domain.requests.UpdateUserRequest.builder()
                .userId(req.getId())
                .firstName(req.getFirstname())
                .lastName(req.getLastname())
                .email(req.getEmail())
                .password(req.getPassword())
                .confirmPassword(req.getConfirmPassword())
                .build();

        return service.updateUser(reqDomain)
                .map(data -> ApiResponseUser.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toUserResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUserDeleteAt> trashedUser(FindByIdUserRequest req) {
        return service.trashUser((long) req.getId())
                .map(data -> ApiResponseUserDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toUserDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUserDeleteAt> restoreUser(FindByIdUserRequest req) {
        return service.restoreUser((long) req.getId())
                .map(data -> ApiResponseUserDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toUserDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUserDelete> deleteUserPermanent(FindByIdUserRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(v -> ApiResponseUserDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("User deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUserAll> restoreAllUser(Empty req) {
        return service.restoreAllUsers()
                .map(v -> ApiResponseUserAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All users restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseUserAll> deleteAllUserPermanent(Empty req) {
        return service.deleteAllPermanentUsers()
                .map(v -> ApiResponseUserAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All users permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

  @Override
  public pb.user.VertxUserCommandServiceGrpcServer.UserCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.Update, this::update);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.TrashedUser, this::trashedUser);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.RestoreUser, this::restoreUser);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.DeleteUserPermanent, this::deleteUserPermanent);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.RestoreAllUser, this::restoreAllUser);
    GrpcServerBinder.bind(server, pb.user.VertxUserCommandServiceGrpcServer.DeleteAllUserPermanent, this::deleteAllUserPermanent);
    return this;
  }
}