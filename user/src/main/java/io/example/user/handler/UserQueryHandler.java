package io.example.user.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.user.domain.requests.FindAllUsers;
import io.example.user.model.UserResponse;
import io.example.user.model.UserResponseDeleteAt;
import io.example.user.service.UserQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.user.UserCommon.ApiResponsePaginationUser;
import pb.user.UserCommon.ApiResponsePaginationUserDeleteAt;
import pb.user.UserCommon.ApiResponseUser;
import pb.user.UserCommon.FindByIdUserRequest;
import pb.user.UserQuery.FindAllUserRequest;

@RequiredArgsConstructor
public class UserQueryHandler implements pb.user.VertxUserQueryServiceGrpcServer.UserQueryServiceApi {
  private final UserQueryService service;

  private FindAllUsers toDomainReq(FindAllUserRequest req) {
    return FindAllUsers.builder()
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
  public Future<ApiResponsePaginationUser> findAll(FindAllUserRequest req) {
    FindAllUsers domainReq = toDomainReq(req);
    Future<PagedResult<UserResponse>> usersFuture = service.getUsers(domainReq);
    return usersFuture
        .map(res -> ApiResponsePaginationUser.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toUserResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponseUser> findById(FindByIdUserRequest req) {
    return service.getUserById((long) req.getId())
        .map(res -> ApiResponseUser.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toUserResponse(res))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponsePaginationUserDeleteAt> findByActive(FindAllUserRequest req) {
    FindAllUsers domainReq = toDomainReq(req);
    Future<PagedResult<UserResponseDeleteAt>> activeUsersFuture = service.getActiveUsers(domainReq);
    return activeUsersFuture
        .map(res -> ApiResponsePaginationUserDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toUserDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }

  @Override
  public Future<ApiResponsePaginationUserDeleteAt> findByTrashed(FindAllUserRequest req) {
    FindAllUsers domainReq = toDomainReq(req);
    Future<PagedResult<UserResponseDeleteAt>> trashedUsersFuture = service.getTrashedUsers(domainReq);
    return trashedUsersFuture
        .map(res -> ApiResponsePaginationUserDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toUserDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
  }
}
