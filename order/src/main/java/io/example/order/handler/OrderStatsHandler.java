package io.example.order.handler;

import io.example.order.service.OrderStatsService;
import io.vertx.core.Future;
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

public class OrderStatsHandler implements OrderStatsServiceApi {
    private final OrderStatsService service;

    public OrderStatsHandler(OrderStatsService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseOrderMonthlyTotalRevenue> findMonthlyTotalRevenue(FindYearMonthTotalRevenue req) {
        return service.getMonthlyTotalRevenue(req.getYear(), req.getMonth())
                .map(resp -> {
                    var builder = ApiResponseOrderMonthlyTotalRevenue.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toMonthlyTotalRevenueResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderYearlyTotalRevenue> findYearlyTotalRevenue(FindYearTotalRevenue req) {
        return service.getYearlyTotalRevenue(req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderYearlyTotalRevenue.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toYearlyTotalRevenueResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderMonthlyTotalRevenue> findMonthlyTotalRevenueByMerchant(FindYearMonthTotalRevenueByMerchant req) {
        return service.getMonthlyTotalRevenueByMerchant(req.getMerchantId(), req.getYear(), req.getMonth())
                .map(resp -> {
                    var builder = ApiResponseOrderMonthlyTotalRevenue.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toMonthlyTotalRevenueResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderYearlyTotalRevenue> findYearlyTotalRevenueByMerchant(FindYearTotalRevenueByMerchant req) {
        return service.getYearlyTotalRevenueByMerchant(req.getMerchantId(), req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderYearlyTotalRevenue.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toYearlyTotalRevenueResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderMonthly> findMonthlyRevenue(FindYearOrder req) {
        return service.getMonthlyOrder(req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderMonthly.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toMonthlyResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderYearly> findYearlyRevenue(FindYearOrder req) {
        return service.getYearlyOrder(req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderYearly.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toYearlyResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderMonthly> findMonthlyRevenueByMerchant(FindYearOrderByMerchant req) {
        return service.getMonthlyOrderByMerchant(req.getMerchantId(), req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderMonthly.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toMonthlyResponse).toList());
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderYearly> findYearlyRevenueByMerchant(FindYearOrderByMerchant req) {
        return service.getYearlyOrderByMerchant(req.getMerchantId(), req.getYear())
                .map(resp -> {
                    var builder = ApiResponseOrderYearly.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.addAllData(resp.data().stream().map(ProtoConverter::toYearlyResponse).toList());
                    }
                    return builder.build();
                });
    }
}
