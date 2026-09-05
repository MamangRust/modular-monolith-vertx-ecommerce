package io.example.order.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.order.domain.requests.*;
import io.example.order.service.OrderStatsService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order.OrderCommon.ApiResponseOrderMonthly;
import pb.order.OrderCommon.ApiResponseOrderYearly;
import pb.order.OrderCommon.ApiResponseOrderMonthlyTotalRevenue;
import pb.order.OrderCommon.ApiResponseOrderYearlyTotalRevenue;
import pb.order.OrderQuery.FindYearMonthTotalRevenue;
import pb.order.OrderQuery.FindYearTotalRevenue;
import pb.order.OrderQuery.FindYearMonthTotalRevenueByMerchant;
import pb.order.OrderQuery.FindYearTotalRevenueByMerchant;
import pb.order.OrderQuery.FindYearOrder;
import pb.order.OrderQuery.FindYearOrderByMerchant;
import pb.order.VertxOrderStatsServiceGrpcServer.OrderStatsServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class OrderStatsHandler implements OrderStatsServiceApi {
    private final OrderStatsService service;

    @Override
    public Future<ApiResponseOrderMonthlyTotalRevenue> findMonthlyTotalRevenue(FindYearMonthTotalRevenue req) {
        return service.getMonthlyTotalRevenue(MonthTotalRevenue.builder()
                .year(req.getYear())
                .month(req.getMonth())
                .build())
                .map(res -> ApiResponseOrderMonthlyTotalRevenue.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toMonthlyTotalRevenueResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderYearlyTotalRevenue> findYearlyTotalRevenue(FindYearTotalRevenue req) {
        return service.getYearlyTotalRevenue(req.getYear())
                .map(res -> ApiResponseOrderYearlyTotalRevenue.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toYearlyTotalRevenueResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderMonthlyTotalRevenue> findMonthlyTotalRevenueByMerchant(
            FindYearMonthTotalRevenueByMerchant req) {
        return service.getMonthlyTotalRevenueByMerchant(MonthTotalRevenueMerchantRequest.builder()
                .merchantId((long) req.getMerchantId())
                .year(req.getYear())
                .month(req.getMonth())
                .build())
                .map(res -> ApiResponseOrderMonthlyTotalRevenue.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toMonthlyTotalRevenueResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderYearlyTotalRevenue> findYearlyTotalRevenueByMerchant(
            FindYearTotalRevenueByMerchant req) {
        return service.getYearlyTotalRevenueByMerchant(YearTotalRevenueMerchantRequest.builder()
                .merchantId((long) req.getMerchantId())
                .year(req.getYear())
                .build())
                .map(res -> ApiResponseOrderYearlyTotalRevenue.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toYearlyTotalRevenueResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderMonthly> findMonthlyRevenue(FindYearOrder req) {
        return service.getMonthlyOrder(req.getYear())
                .map(res -> ApiResponseOrderMonthly.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toMonthlyResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderYearly> findYearlyRevenue(FindYearOrder req) {
        return service.getYearlyOrder(req.getYear())
                .map(res -> ApiResponseOrderYearly.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toYearlyResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderMonthly> findMonthlyRevenueByMerchant(FindYearOrderByMerchant req) {
        return service.getMonthlyOrderByMerchant(MonthOrderMerchantRequest.builder()
                .merchantId((long) req.getMerchantId())
                .year(req.getYear())
                .build())
                .map(res -> ApiResponseOrderMonthly.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toMonthlyResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<ApiResponseOrderYearly> findYearlyRevenueByMerchant(FindYearOrderByMerchant req) {
        return service.getYearlyOrderByMerchant(YearOrderMerchantRequest.builder()
                .merchantId((long) req.getMerchantId())
                .year(req.getYear())
                .build())
                .map(res -> ApiResponseOrderYearly.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toYearlyResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

  @Override
  public pb.order.VertxOrderStatsServiceGrpcServer.OrderStatsServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindMonthlyTotalRevenue, this::findMonthlyTotalRevenue);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindYearlyTotalRevenue, this::findYearlyTotalRevenue);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindMonthlyTotalRevenueByMerchant, this::findMonthlyTotalRevenueByMerchant);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindYearlyTotalRevenueByMerchant, this::findYearlyTotalRevenueByMerchant);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindMonthlyRevenue, this::findMonthlyRevenue);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindYearlyRevenue, this::findYearlyRevenue);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindMonthlyRevenueByMerchant, this::findMonthlyRevenueByMerchant);
    GrpcServerBinder.bind(server, pb.order.VertxOrderStatsServiceGrpcServer.FindYearlyRevenueByMerchant, this::findYearlyRevenueByMerchant);
    return this;
  }
}