package io.example.order.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.order.domain.requests.FindAllOrderRequest;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.service.OrderQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order.OrderCommon.ApiResponseOrder;
import pb.order.OrderCommon.ApiResponsePaginationOrder;
import pb.order.OrderCommon.ApiResponsePaginationOrderDeleteAt;
import pb.order.OrderCommon.FindByIdOrderRequest;
import pb.order.VertxOrderQueryServiceGrpcServer.OrderQueryServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class OrderQueryHandler implements OrderQueryServiceApi {
    private final OrderQueryService service;

    private FindAllOrderRequest toDomainReq(pb.order.OrderQuery.FindAllOrderRequest req) {
        return FindAllOrderRequest.builder()
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
    public Future<ApiResponsePaginationOrder> findAll(pb.order.OrderQuery.FindAllOrderRequest req) {
        FindAllOrderRequest domainReq = toDomainReq(req);
        Future<PagedResult<OrderResponse>> ordersFuture = service.getAll(domainReq);
        return ordersFuture
                .map(res -> ApiResponsePaginationOrder.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toOrderResponse).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrder> findById(FindByIdOrderRequest req) {
        return service.getById((long) req.getId())
                .map(res -> ApiResponseOrder.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toOrderResponse(res))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponsePaginationOrderDeleteAt> findByActive(pb.order.OrderQuery.FindAllOrderRequest req) {
        FindAllOrderRequest domainReq = toDomainReq(req);
        Future<PagedResult<OrderResponse>> activeOrdersFuture = service.getActive(domainReq);
        return activeOrdersFuture
                .map(res -> ApiResponsePaginationOrderDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toOrderResponseDeleteAt).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponsePaginationOrderDeleteAt> findByTrashed(pb.order.OrderQuery.FindAllOrderRequest req) {
        FindAllOrderRequest domainReq = toDomainReq(req);
        Future<PagedResult<OrderResponseDeleteAt>> trashedOrdersFuture = service.getTrashed(domainReq);
        return trashedOrdersFuture
                .map(res -> ApiResponsePaginationOrderDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toOrderResponseDeleteAt).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

  @Override
  public pb.order.VertxOrderQueryServiceGrpcServer.OrderQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.order.VertxOrderQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.order.VertxOrderQueryServiceGrpcServer.FindById, this::findById);
    GrpcServerBinder.bind(server, pb.order.VertxOrderQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.order.VertxOrderQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    return this;
  }
}
