package io.example.category.handler;

import java.util.List;

import io.example.category.service.CategoryStatsByIdService;
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
class CategoryStatsByIdHandlerTest {

    @Mock
    private CategoryStatsByIdService service;

    private CategoryStatsByIdHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CategoryStatsByIdHandler(service);
    }

    @Test
    @DisplayName("findMonthlyTotalPricesById delegates and returns success")
    void findMonthlyTotalPricesById(VertxTestContext ctx) {
        when(service.getMonthlyTotalPriceById(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearMonthTotalPriceById.newBuilder()
                .setYear(2024)
                .setMonth(10)
                .setCategoryId(1)
                .build();

        handler.findMonthlyTotalPricesById(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(0);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthlyTotalPricesById delegates error when service fails")
    void findMonthlyTotalPricesByIdError(VertxTestContext ctx) {
        when(service.getMonthlyTotalPriceById(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearMonthTotalPriceById.newBuilder().build();

        handler.findMonthlyTotalPricesById(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalPricesById delegates and returns success")
    void findYearlyTotalPricesById(VertxTestContext ctx) {
        when(service.getYearlyTotalPriceById(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearTotalPriceById.newBuilder()
                .setYear(2024)
                .setCategoryId(1)
                .build();

        handler.findYearlyTotalPricesById(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearlyTotalPricesById delegates error when service fails")
    void findYearlyTotalPricesByIdError(VertxTestContext ctx) {
        when(service.getYearlyTotalPriceById(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearTotalPriceById.newBuilder().build();

        handler.findYearlyTotalPricesById(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthPriceById delegates and returns success")
    void findMonthPriceById(VertxTestContext ctx) {
        when(service.getMonthlyCategoryById(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearCategoryById.newBuilder()
                .setYear(2024)
                .setCategoryId(1)
                .build();

        handler.findMonthPriceById(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findMonthPriceById delegates error when service fails")
    void findMonthPriceByIdError(VertxTestContext ctx) {
        when(service.getMonthlyCategoryById(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearCategoryById.newBuilder().build();

        handler.findMonthPriceById(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearPriceById delegates and returns success")
    void findYearPriceById(VertxTestContext ctx) {
        when(service.getYearlyCategoryById(any())).thenReturn(Future.succeededFuture(List.of()));

        var req = CategoryCommon.FindYearCategoryById.newBuilder()
                .setYear(2024)
                .setCategoryId(1)
                .build();

        handler.findYearPriceById(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findYearPriceById delegates error when service fails")
    void findYearPriceByIdError(VertxTestContext ctx) {
        when(service.getYearlyCategoryById(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindYearCategoryById.newBuilder().build();

        handler.findYearPriceById(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}