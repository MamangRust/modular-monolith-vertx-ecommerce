package io.example.order_item.handler;

import io.example.order_item.service.OrderItemQueryService;
import io.vertx.core.Future;
import pb.order_item.OrderItemQuery.FindAllOrderItemRequest;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.OrderItemCommon.ApiResponsePaginationOrderItem;
import pb.order_item.OrderItemCommon.ApiResponsePaginationOrderItemDeleteAt;
import pb.order_item.OrderItemCommon.ApiResponsesOrderItem;
import pb.order_item.VertxOrderItemQueryServiceGrpcServer.OrderItemQueryServiceApi;

public class OrderItemQueryHandler implements OrderItemQueryServiceApi {
    private final OrderItemQueryService service;

    public OrderItemQueryHandler(OrderItemQueryService service) {
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
    public Future<ApiResponsePaginationOrderItem> findAll(FindAllOrderItemRequest req) {
        return service.getAll(req)
                .map(resp -> ApiResponsePaginationOrderItem.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromModel).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponsePaginationOrderItemDeleteAt> findByActive(FindAllOrderItemRequest req) {
        return service.getActive(req)
                .map(resp -> ApiResponsePaginationOrderItemDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromModelDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponsePaginationOrderItemDeleteAt> findByTrashed(FindAllOrderItemRequest req) {
        return service.getTrashed(req)
                .map(resp -> ApiResponsePaginationOrderItemDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromModelDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponsesOrderItem> findOrderItemByOrder(FindByIdOrderItemRequest req) {
        return service.getByOrderId(req.getId())
                .map(resp -> {
                    ApiResponsesOrderItem.Builder builder = ApiResponsesOrderItem.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::fromModel).toList());
                    }
                    return builder.build();
                });
    }
}
