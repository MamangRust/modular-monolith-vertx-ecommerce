package io.example.order.handler;

import java.util.List;

import io.example.order.service.OrderStatsService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.order.OrderQuery.FindYearMonthTotalRevenue;
import pb.order.OrderQuery.FindYearTotalRevenue;
import pb.order.OrderQuery.FindYearMonthTotalRevenueByMerchant;
import pb.order.OrderQuery.FindYearTotalRevenueByMerchant;
import pb.order.OrderQuery.FindYearOrder;
import pb.order.OrderQuery.FindYearOrderByMerchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class OrderStatsHandlerTest {

    @Mock
    private OrderStatsService service;

    private OrderStatsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderStatsHandler(service);
    }

    @Test
    @DisplayName("findMonthlyTotalRevenue delegates and returns success")
    void findMonthlyTotalRevenue(VertxTestContext ctx) {
        when(service.getMonthlyTotalRevenue(any())).thenReturn(Future.succeededFuture(List.of()));

        handler.findMonthlyTotalRevenue(FindYearMonthTotalRevenue.newBuilder().setYear(2024).setMonth(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(0);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyTotalRevenue delegates error when service fails")
    void findMonthlyTotalRevenueError(VertxTestContext ctx) {
        when(service.getMonthlyTotalRevenue(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findMonthlyTotalRevenue(FindYearMonthTotalRevenue.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalRevenue delegates and returns success")
    void findYearlyTotalRevenue(VertxTestContext ctx) {
        when(service.getYearlyTotalRevenue(anyInt())).thenReturn(Future.succeededFuture(List.of()));

        handler.findYearlyTotalRevenue(FindYearTotalRevenue.newBuilder().setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalRevenue delegates error when service fails")
    void findYearlyTotalRevenueError(VertxTestContext ctx) {
        when(service.getYearlyTotalRevenue(anyInt())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findYearlyTotalRevenue(FindYearTotalRevenue.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyTotalRevenueByMerchant delegates and returns success")
    void findMonthlyTotalRevenueByMerchant(VertxTestContext ctx) {
        when(service.getMonthlyTotalRevenueByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        handler.findMonthlyTotalRevenueByMerchant(FindYearMonthTotalRevenueByMerchant.newBuilder().setMerchantId(1).setYear(2024).setMonth(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyTotalRevenueByMerchant delegates error when service fails")
    void findMonthlyTotalRevenueByMerchantError(VertxTestContext ctx) {
        when(service.getMonthlyTotalRevenueByMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findMonthlyTotalRevenueByMerchant(FindYearMonthTotalRevenueByMerchant.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalRevenueByMerchant delegates and returns success")
    void findYearlyTotalRevenueByMerchant(VertxTestContext ctx) {
        when(service.getYearlyTotalRevenueByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        handler.findYearlyTotalRevenueByMerchant(FindYearTotalRevenueByMerchant.newBuilder().setMerchantId(1).setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalRevenueByMerchant delegates error when service fails")
    void findYearlyTotalRevenueByMerchantError(VertxTestContext ctx) {
        when(service.getYearlyTotalRevenueByMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findYearlyTotalRevenueByMerchant(FindYearTotalRevenueByMerchant.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyRevenue delegates and returns success")
    void findMonthlyRevenue(VertxTestContext ctx) {
        when(service.getMonthlyOrder(anyInt())).thenReturn(Future.succeededFuture(List.of()));

        handler.findMonthlyRevenue(FindYearOrder.newBuilder().setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyRevenue delegates error when service fails")
    void findMonthlyRevenueError(VertxTestContext ctx) {
        when(service.getMonthlyOrder(anyInt())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findMonthlyRevenue(FindYearOrder.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyRevenue delegates and returns success")
    void findYearlyRevenue(VertxTestContext ctx) {
        when(service.getYearlyOrder(anyInt())).thenReturn(Future.succeededFuture(List.of()));

        handler.findYearlyRevenue(FindYearOrder.newBuilder().setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyRevenue delegates error when service fails")
    void findYearlyRevenueError(VertxTestContext ctx) {
        when(service.getYearlyOrder(anyInt())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findYearlyRevenue(FindYearOrder.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyRevenueByMerchant delegates and returns success")
    void findMonthlyRevenueByMerchant(VertxTestContext ctx) {
        when(service.getMonthlyOrderByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        handler.findMonthlyRevenueByMerchant(FindYearOrderByMerchant.newBuilder().setMerchantId(1).setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyRevenueByMerchant delegates error when service fails")
    void findMonthlyRevenueByMerchantError(VertxTestContext ctx) {
        when(service.getMonthlyOrderByMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findMonthlyRevenueByMerchant(FindYearOrderByMerchant.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyRevenueByMerchant delegates and returns success")
    void findYearlyRevenueByMerchant(VertxTestContext ctx) {
        when(service.getYearlyOrderByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        handler.findYearlyRevenueByMerchant(FindYearOrderByMerchant.newBuilder().setMerchantId(1).setYear(2024).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyRevenueByMerchant delegates error when service fails")
    void findYearlyRevenueByMerchantError(VertxTestContext ctx) {
        when(service.getYearlyOrderByMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findYearlyRevenueByMerchant(FindYearOrderByMerchant.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}