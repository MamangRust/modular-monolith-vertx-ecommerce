package io.example.merchant_detail.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantDetailQueryHandlerTest {

    @Mock
    private MerchantDetailQueryService service;

    private MerchantDetailQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantDetailQueryHandler(service);
    }

    private static MerchantDetailResponse aDetailResponse() {
        return MerchantDetailResponse.builder()
                .id(1l)
                .merchantId(10)
                .displayName("Toko Sejahtera Maju")
                .coverImageUrl("https://url.com/cover.png")
                .logoUrl("https://url.com/logo.png")
                .shortDescription("Toko terbaik sejak 2020")
                .websiteUrl("https://tokosejahtera.com")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static MerchantDetailResponseDeleteAt aDetailResponseDeleteAt() {
        return MerchantDetailResponseDeleteAt.builder()
                .id(1l)
                .merchantId(10)
                .displayName("Toko Sejahtera Maju")
                .coverImageUrl("https://url.com/cover.png")
                .logoUrl("https://url.com/logo.png")
                .shortDescription("Toko terbaik sejak 2020")
                .websiteUrl("https://tokosejahtera.com")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    private static PagedResult<MerchantDetailResponse> aPagedResultDetail() {
        return new PagedResult<>(List.of(aDetailResponse()), 1);
    }

    private static PagedResult<MerchantDetailResponseDeleteAt> aPagedResultDetailDeleteAt() {
        return new PagedResult<>(List.of(aDetailResponseDeleteAt()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getMerchantDetails(any())).thenReturn(Future.succeededFuture(aPagedResultDetail()));

        var req = FindAllMerchantRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .setSearch("Sejahtera")
                .build();

        handler.findAll(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAll delegates error when service fails")
    void findAllError(VertxTestContext ctx) {
        when(service.getMerchantDetails(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findAll(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(service.getMerchantDetail(anyLong())).thenReturn(Future.succeededFuture(aDetailResponse()));

        handler.findById(FindByIdMerchantDetailRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getDisplayName()).isEqualTo("Toko Sejahtera Maju");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getMerchantDetail(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        handler.findById(FindByIdMerchantDetailRequest.newBuilder().setId(99).build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getMerchantDetailsActive(any())).thenReturn(Future.succeededFuture(aPagedResultDetailDeleteAt()));

        handler.findByActive(FindAllMerchantRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates error when service fails")
    void findByActiveError(VertxTestContext ctx) {
        when(service.getMerchantDetailsActive(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByActive(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getMerchantDetailsTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultDetailDeleteAt()));

        handler.findByTrashed(FindAllMerchantRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates error when service fails")
    void findByTrashedError(VertxTestContext ctx) {
        when(service.getMerchantDetailsTrashed(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByTrashed(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}