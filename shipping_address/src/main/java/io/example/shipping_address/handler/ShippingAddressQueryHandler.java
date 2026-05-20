package io.example.shipping_address.handler;

import io.example.shipping_address.model.FindAllShippingAddress;
import io.example.shipping_address.service.ShippingAddressQueryService;
import io.vertx.core.Future;
import pb.shipping_address.ShippingAddressCommon.ApiResponsePaginationShipping;
import pb.shipping_address.ShippingAddressCommon.ApiResponsePaginationShippingDeleteAt;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShipping;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;

public class ShippingAddressQueryHandler implements pb.shipping_address.VertxShippingQueryServiceGrpcServer.ShippingQueryServiceApi {
    private final ShippingAddressQueryService service;

    public ShippingAddressQueryHandler(ShippingAddressQueryService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponsePaginationShipping> findAll(pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress reqDto = FindAllShippingAddress.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getAllShippingAddresses(reqDto)
                .map(res -> {
                    ApiResponsePaginationShipping.Builder builder = ApiResponsePaginationShipping.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponse).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShipping> findById(FindByIdShippingRequest req) {
        return service.getShippingAddressById(req.getId())
                .map(res -> {
                    ApiResponseShipping.Builder builder = ApiResponseShipping.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShipping> findByOrder(FindByIdShippingRequest req) {
        return service.getShippingAddressByOrderId(req.getId())
                .map(res -> {
                    ApiResponseShipping.Builder builder = ApiResponseShipping.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationShippingDeleteAt> findByActive(pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress reqDto = FindAllShippingAddress.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getActiveShippingAddresses(reqDto)
                .map(res -> {
                    ApiResponsePaginationShippingDeleteAt.Builder builder = ApiResponsePaginationShippingDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationShippingDeleteAt> findByTrashed(pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress reqDto = FindAllShippingAddress.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTrashedShippingAddresses(reqDto)
                .map(res -> {
                    ApiResponsePaginationShippingDeleteAt.Builder builder = ApiResponsePaginationShippingDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }
}
