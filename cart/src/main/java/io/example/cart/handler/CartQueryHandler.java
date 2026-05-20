package io.example.cart.handler;

import io.example.cart.service.CartQueryService;
import io.vertx.core.Future;
import pb.cart.CartCommon.ApiResponsePaginationCart;
import pb.cart.CartQuery.FindAllCartRequest;

public class CartQueryHandler implements pb.cart.VertxCartQueryServiceGrpcServer.CartQueryServiceApi {
    private final CartQueryService service;

    public CartQueryHandler(CartQueryService service) {
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
    public Future<ApiResponsePaginationCart> findAll(FindAllCartRequest req) {
        return service.findAll(req)
                .map(resp -> {
                    var builder = ApiResponsePaginationCart.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "")
                            .setPagination(toMeta(resp.pagination()));
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toProto).toList());
                    }
                    return builder.build();
                });
    }
}
