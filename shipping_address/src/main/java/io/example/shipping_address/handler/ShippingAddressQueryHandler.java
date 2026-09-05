package io.example.shipping_address.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.shipping_address.domain.requests.FindAllShippingAddress;
import io.example.shipping_address.service.ShippingAddressQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.shipping_address.ShippingAddressCommon.ApiResponsePaginationShipping;
import pb.shipping_address.ShippingAddressCommon.ApiResponsePaginationShippingDeleteAt;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShipping;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class ShippingAddressQueryHandler
        implements pb.shipping_address.VertxShippingQueryServiceGrpcServer.ShippingQueryServiceApi {
    private final ShippingAddressQueryService service;

    private FindAllShippingAddress toDomainReq(
            pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        return FindAllShippingAddress.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
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
    public Future<ApiResponsePaginationShipping> findAll(
            pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress domainReq = toDomainReq(req);
        return service.getAllShippingAddresses(domainReq)
                .map(res -> ApiResponsePaginationShipping.newBuilder()
                        .setStatus("success").setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponsePaginationShippingDeleteAt> findByActive(
            pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress domainReq = toDomainReq(req);
        return service.getActiveShippingAddresses(domainReq)
                .map(res -> ApiResponsePaginationShippingDeleteAt.newBuilder()
                        .setStatus("success").setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponsePaginationShippingDeleteAt> findByTrashed(
            pb.shipping_address.ShippingAddressQuery.FindAllShippingRequest req) {
        FindAllShippingAddress domainReq = toDomainReq(req);
        return service.getTrashedShippingAddresses(domainReq)
                .map(res -> ApiResponsePaginationShippingDeleteAt.newBuilder()
                        .setStatus("success").setMessage("OK")
                        .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
                        .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseShipping> findById(FindByIdShippingRequest req) {
        return service.getShippingAddressById((long) req.getId())
                .map(data -> ApiResponseShipping.newBuilder()
                        .setStatus("success").setMessage("OK")
                        .setData(ProtoConverter.toProtoResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseShipping> findByOrder(FindByIdShippingRequest req) {
        return service.getShippingAddressByOrderId((long) req.getId())
                .map(data -> ApiResponseShipping.newBuilder()
                        .setStatus("success").setMessage("OK")
                        .setData(ProtoConverter.toProtoResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

  @Override
  public pb.shipping_address.VertxShippingQueryServiceGrpcServer.ShippingQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingQueryServiceGrpcServer.FindById, this::findById);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingQueryServiceGrpcServer.FindByOrder, this::findByOrder);
    return this;
  }
}