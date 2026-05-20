package io.example.order.handler;

import io.example.order.service.OrderQueryService;
import io.vertx.core.Future;
import pb.order.OrderCommon.ApiResponseOrder;
import pb.order.OrderCommon.ApiResponsePaginationOrder;
import pb.order.OrderCommon.ApiResponsePaginationOrderDeleteAt;
import pb.order.OrderCommon.FindByIdOrderRequest;
import pb.order.OrderQuery.FindAllOrderRequest;
import pb.order.VertxOrderQueryServiceGrpcServer.OrderQueryServiceApi;

public class OrderQueryHandler implements OrderQueryServiceApi {
    private final OrderQueryService service;

    public OrderQueryHandler(OrderQueryService service) {
        this.service = service;
    }

    private pb.Api.PaginationMeta toMeta(io.example.common.model.PaginationMeta meta) {
        if (meta == null) {
            return pb.Api.PaginationMeta.getDefaultInstance();
        }
        return pb.Api.PaginationMeta.newBuilder()
                .setCurrentPage(meta.currentPage())
                .setPageSize(meta.pageSize())
                .setTotalPages(meta.totalPages())
                .setTotalRecords(meta.totalRecords())
                .build();
    }

    @Override
    public Future<ApiResponsePaginationOrder> findAll(FindAllOrderRequest req) {
        return service.getAll(req.getSearch(), req.getPage(), req.getPageSize())
                .map(resp -> ApiResponsePaginationOrder.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toOrderResponse).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponseOrder> findById(FindByIdOrderRequest req) {
        return service.getById((long) req.getId())
                .map(resp -> {
                    var builder = ApiResponseOrder.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toOrderResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationOrderDeleteAt> findByActive(FindAllOrderRequest req) {
        return service.getActive(req.getSearch(), req.getPage(), req.getPageSize())
                .map(resp -> ApiResponsePaginationOrderDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toOrderResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponsePaginationOrderDeleteAt> findByTrashed(FindAllOrderRequest req) {
        return service.getTrashed(req.getSearch(), req.getPage(), req.getPageSize())
                .map(resp -> ApiResponsePaginationOrderDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toOrderResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }
}
