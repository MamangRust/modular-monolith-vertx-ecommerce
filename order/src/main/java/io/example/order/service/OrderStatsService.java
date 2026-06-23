package io.example.order.service;

import java.util.List;

import io.example.order.domain.requests.*;
import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.vertx.core.Future;

public interface OrderStatsService {
    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenue(MonthTotalRevenue req);

    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenue(int year);

    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueById(MonthTotalRevenueByIdRequest req);

    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueById(YearTotalRevenueByIdRequest req);

    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueByMerchant(MonthTotalRevenueMerchantRequest req);

    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueByMerchant(YearTotalRevenueMerchantRequest req);

    Future<List<OrderMonthly>> getMonthlyOrder(int year);

    Future<List<OrderYearly>> getYearlyOrder(int year);

    Future<List<OrderMonthly>> getMonthlyOrderByMerchant(MonthOrderMerchantRequest req);

    Future<List<OrderYearly>> getYearlyOrderByMerchant(YearOrderMerchantRequest req);
}