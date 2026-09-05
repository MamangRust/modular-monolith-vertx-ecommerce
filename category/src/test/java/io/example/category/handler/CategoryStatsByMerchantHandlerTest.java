package io.example.category.handler;

import java.util.List;

import io.example.category.service.CategoryStatsByMerchantService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.category.CategoryCommon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class CategoryStatsByMerchantHandlerTest {

    @Mock
    private CategoryStatsByMerchantService service;

    private CategoryStatsByMerchantHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CategoryStatsByMerchantHandler(service);
    }

    @Test
    @DisplayName("findMonthlyTotalPricesByMerchant delegates and returns success")
    void findMonthlyTotalPricesByMerchant(VertxTestContext ctx) {
        when(service.getMonthlyTotalPriceByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearMonthTotalPriceByMerchant.newBuilder()
                .setYear(2024)
                .setMonth(10)
                .setMerchantId(1)
                .build();

        handler.findMonthlyTotalPricesByMerchant(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(0);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyTotalPricesByMerchant delegates error when service fails")
    void findMonthlyTotalPricesByMerchantError(VertxTestContext ctx) {
        when(service.getMonthlyTotalPriceByMerchant(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearMonthTotalPriceByMerchant.newBuilder().build();

        handler.findMonthlyTotalPricesByMerchant(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalPricesByMerchant delegates and returns success")
    void findYearlyTotalPricesByMerchant(VertxTestContext ctx) {
        when(service.getYearlyTotalPriceByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearTotalPriceByMerchant.newBuilder()
                .setYear(2024)
                .setMerchantId(1)
                .build();

        handler.findYearlyTotalPricesByMerchant(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalPricesByMerchant delegates error when service fails")
    void findYearlyTotalPricesByMerchantError(VertxTestContext ctx) {
        when(service.getYearlyTotalPriceByMerchant(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearTotalPriceByMerchant.newBuilder().build();

        handler.findYearlyTotalPricesByMerchant(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthPriceByMerchant delegates and returns success")
    void findMonthPriceByMerchant(VertxTestContext ctx) {
        when(service.getMonthlyCategoryByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                .setYear(2024)
                .setMerchantId(1)
                .build();

        handler.findMonthPriceByMerchant(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthPriceByMerchant delegates error when service fails")
    void findMonthPriceByMerchantError(VertxTestContext ctx) {
        when(service.getMonthlyCategoryByMerchant(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder().build();

        handler.findMonthPriceByMerchant(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearPriceByMerchant delegates and returns success")
    void findYearPriceByMerchant(VertxTestContext ctx) {
        when(service.getYearlyCategoryByMerchant(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                .setYear(2024)
                .setMerchantId(1)
                .build();

        handler.findYearPriceByMerchant(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearPriceByMerchant delegates error when service fails")
    void findYearPriceByMerchantError(VertxTestContext ctx) {
        when(service.getYearlyCategoryByMerchant(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder().build();

        handler.findYearPriceByMerchant(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}