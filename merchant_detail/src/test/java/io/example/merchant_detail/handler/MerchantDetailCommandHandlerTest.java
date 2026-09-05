package io.example.merchant_detail.handler;

import com.google.protobuf.Empty;

import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantDetailCommandHandlerTest {

    @Mock
    private MerchantDetailCommandService service;

    private MerchantDetailCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantDetailCommandHandler(service);
    }

    private static MerchantDetailResponse aDetailResponse() {
        return MerchantDetailResponse.builder()
                .id(1L)
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
                .id(1L)
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

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(aDetailResponse()));

        var req = pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest.newBuilder()
                .setMerchantId(10)
                .setDisplayName("Toko Sejahtera Maju")
                .setCoverImageUrl("https://url.com/cover.png")
                .setLogoUrl("https://url.com/logo.png")
                .setShortDescription("Toko terbaik sejak 2020")
                .setWebsiteUrl("https://tokosejahtera.com")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getDisplayName()).isEqualTo("Toko Sejahtera Maju");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(aDetailResponse()));

        var req = pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest.newBuilder()
                .setMerchantDetailId(1)
                .setDisplayName("Toko Baru")
                .build();

        handler.update(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates error when service fails")
    void updateError(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.update(pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantDetail delegates and returns response")
    void trashedMerchantDetail(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(aDetailResponseDeleteAt()));

        handler.trashedMerchantDetail(FindByIdMerchantDetailRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantDetail delegates error when service fails")
    void trashedMerchantDetailError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedMerchantDetail(FindByIdMerchantDetailRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantDetail delegates and returns response")
    void restoreMerchantDetail(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(aDetailResponseDeleteAt()));

        handler.restoreMerchantDetail(FindByIdMerchantDetailRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantDetail delegates error when service fails")
    void restoreMerchantDetailError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreMerchantDetail(FindByIdMerchantDetailRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantDetailPermanent delegates and returns success")
    void deleteMerchantDetailPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteMerchantDetailPermanent(FindByIdMerchantDetailRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Merchant detail deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantDetailPermanent delegates error when service fails")
    void deleteMerchantDetailPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteMerchantDetailPermanent(FindByIdMerchantDetailRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantDetail delegates and returns success")
    void restoreAllMerchantDetail(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllMerchantDetail(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchant details restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantDetail delegates error when service fails")
    void restoreAllMerchantDetailError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllMerchantDetail(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantDetailPermanent delegates and returns success")
    void deleteAllMerchantDetailPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllMerchantDetailPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchant details permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantDetailPermanent delegates error when service fails")
    void deleteAllMerchantDetailPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllMerchantDetailPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}