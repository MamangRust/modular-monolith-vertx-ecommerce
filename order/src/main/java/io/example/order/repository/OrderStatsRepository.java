package io.example.order.repository;

import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.vertx.core.Future;
import java.util.List;

public interface OrderStatsRepository {
    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenue(int year, int month);
    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenue(int year);
    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueById(Long orderId, int year, int month);
    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueById(Long orderId, int year);
    Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueByMerchant(Integer merchantId, int year, int month);
    Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueByMerchant(Integer merchantId, int year);

    Future<List<OrderMonthly>> getMonthlyOrder(int year);
    Future<List<OrderYearly>> getYearlyOrder(int year);
    Future<List<OrderMonthly>> getMonthlyOrderByMerchant(Integer merchantId, int year);
    Future<List<OrderYearly>> getYearlyOrderByMerchant(Integer merchantId, int year);
}
