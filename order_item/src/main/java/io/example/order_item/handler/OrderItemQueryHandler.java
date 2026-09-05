package io.example.order_item.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.order_item.domain.requests.FindAllOrderItemRequest;
import io.example.order_item.service.OrderItemQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order_item.OrderItemCommon.ApiResponsePaginationOrderItem;
import pb.order_item.OrderItemCommon.ApiResponsePaginationOrderItemDeleteAt;
import pb.order_item.OrderItemCommon.ApiResponsesOrderItem;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.VertxOrderItemQueryServiceGrpcServer.OrderItemQueryServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class OrderItemQueryHandler implements OrderItemQueryServiceApi {
        private final OrderItemQueryService service;

        private FindAllOrderItemRequest toDomainReq(pb.order_item.OrderItemQuery.FindAllOrderItemRequest req) {
                return FindAllOrderItemRequest.builder()
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .search(req.getSearch())
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
        public Future<ApiResponsePaginationOrderItem> findAll(
                        pb.order_item.OrderItemQuery.FindAllOrderItemRequest req) {
                FindAllOrderItemRequest domainReq = toDomainReq(req);
                return service.getAll(domainReq)
                                .map(res -> ApiResponsePaginationOrderItem.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::fromModel)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationOrderItemDeleteAt> findByActive(
                        pb.order_item.OrderItemQuery.FindAllOrderItemRequest req) {
                FindAllOrderItemRequest domainReq = toDomainReq(req);
                return service.getActive(domainReq)
                                .map(res -> ApiResponsePaginationOrderItemDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromModelDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationOrderItemDeleteAt> findByTrashed(
                        pb.order_item.OrderItemQuery.FindAllOrderItemRequest req) {
                FindAllOrderItemRequest domainReq = toDomainReq(req);
                return service.getTrashed(domainReq)
                                .map(res -> ApiResponsePaginationOrderItemDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromModelDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsesOrderItem> findOrderItemByOrder(FindByIdOrderItemRequest req) {
                return service.getByOrderId((long) req.getId())
                                .map(res -> ApiResponsesOrderItem.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.stream().map(ProtoConverter::fromModel).toList())
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.order_item.VertxOrderItemQueryServiceGrpcServer.OrderItemQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemQueryServiceGrpcServer.FindOrderItemByOrder, this::findOrderItemByOrder);
    return this;
  }
}