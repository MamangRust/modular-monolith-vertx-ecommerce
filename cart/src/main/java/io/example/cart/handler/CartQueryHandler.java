package io.example.cart.handler;

import io.example.cart.domain.requests.FindAllCartsRequest;
import io.example.cart.service.CartQueryService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.cart.CartCommon.ApiResponsePaginationCart;
import pb.cart.CartQuery.FindAllCartRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class CartQueryHandler implements pb.cart.VertxCartQueryServiceGrpcServer.CartQueryServiceApi {
    private final CartQueryService service;

    private FindAllCartsRequest toDomainReq(FindAllCartRequest req) {
        return FindAllCartsRequest.builder()
                .userId(req.getUserId())
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
    public Future<ApiResponsePaginationCart> findAll(FindAllCartRequest req) {
        FindAllCartsRequest domainReq = toDomainReq(req);
        return service.findAll(domainReq)
                .map(res -> ApiResponsePaginationCart.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toProto).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

  @Override
  public pb.cart.VertxCartQueryServiceGrpcServer.CartQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.cart.VertxCartQueryServiceGrpcServer.FindAll, this::findAll);
    return this;
  }
}