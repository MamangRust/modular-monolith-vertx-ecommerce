package io.example.order.service;

import io.example.common.model.ApiResponse;
import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.vertx.core.Future;
import java.util.List;

public interface OrderStatsService {
    Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenue(int year, int month);
    Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenue(int year);
    Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenueById(Long orderId, int year, int month);
    Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenueById(Long orderId, int year);
    Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenueByMerchant(Integer merchantId, int year, int month);
    Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenueByMerchant(Integer merchantId, int year);

    Future<ApiResponse<List<OrderMonthly>>> getMonthlyOrder(int year);
    Future<ApiResponse<List<OrderYearly>>> getYearlyOrder(int year);
    Future<ApiResponse<List<OrderMonthly>>> getMonthlyOrderByMerchant(Integer merchantId, int year);
    Future<ApiResponse<List<OrderYearly>>> getYearlyOrderByMerchant(Integer merchantId, int year);
}
