package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.service.MerchantCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant.MerchantCommand.CreateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantStatusRequest;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantCommandHandlerTest {

    @Mock
    private MerchantCommandService service;

    private MerchantCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantCommandHandler(service);
    }

    private static MerchantResponse aMerchantResponse() {
        return MerchantResponse.builder()
                .id(1)
                .userId(10)
                .name("Toko Sejahtera")
                .status("ACTIVE")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .build();
    }

    private static MerchantResponseDeleteAt aMerchantResponseDeleteAt() {
        return MerchantResponseDeleteAt.builder()
                .id(1)
                .userId(10)
                .name("Toko Sejahtera")
                .status("ACTIVE")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .deletedAt("2026-06-25T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.createMerchant(any())).thenReturn(Future.succeededFuture(aMerchantResponse()));

        var req = CreateMerchantRequest.newBuilder()
                .setUserId(10)
                .setName("Toko Sejahtera")
                .setStatus("ACTIVE")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.createMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.create(CreateMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.updateMerchant(any())).thenReturn(Future.succeededFuture(aMerchantResponse()));

        handler.update(UpdateMerchantRequest.newBuilder().setMerchantId(1).setName("Toku Baru").build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates error when service fails")
    void updateError(VertxTestContext ctx) {
        when(service.updateMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.update(UpdateMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateStatus delegates and returns response")
    void updateStatus(VertxTestContext ctx) {
        when(service.updateStatus(any())).thenReturn(Future.succeededFuture(aMerchantResponse()));

        handler.updateStatus(UpdateMerchantStatusRequest.newBuilder().setMerchantId(1).setStatus("SUSPENDED").build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateStatus delegates error when service fails")
    void updateStatusError(VertxTestContext ctx) {
        when(service.updateStatus(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.updateStatus(UpdateMerchantStatusRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchant delegates and returns response")
    void trashedMerchant(VertxTestContext ctx) {
        when(service.trashMerchant(anyLong())).thenReturn(Future.succeededFuture(aMerchantResponseDeleteAt()));

        handler.trashedMerchant(FindByIdMerchantRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchant delegates error when service fails")
    void trashedMerchantError(VertxTestContext ctx) {
        when(service.trashMerchant(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.trashedMerchant(FindByIdMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchant delegates and returns response")
    void restoreMerchant(VertxTestContext ctx) {
        when(service.restoreMerchant(anyLong())).thenReturn(Future.succeededFuture(aMerchantResponse()));

        handler.restoreMerchant(FindByIdMerchantRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchant delegates error when service fails")
    void restoreMerchantError(VertxTestContext ctx) {
        when(service.restoreMerchant(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.restoreMerchant(FindByIdMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantPermanent delegates and returns success")
    void deleteMerchantPermanent(VertxTestContext ctx) {
        when(service.deleteMerchantPermanently(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteMerchantPermanent(FindByIdMerchantRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Merchant deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantPermanent delegates error when service fails")
    void deleteMerchantPermanentError(VertxTestContext ctx) {
        when(service.deleteMerchantPermanently(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.deleteMerchantPermanent(FindByIdMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchant delegates and returns success")
    void restoreAllMerchant(VertxTestContext ctx) {
        when(service.restoreAllMerchants()).thenReturn(Future.succeededFuture());

        handler.restoreAllMerchant(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchants restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchant delegates error when service fails")
    void restoreAllMerchantError(VertxTestContext ctx) {
        when(service.restoreAllMerchants()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.restoreAllMerchant(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantPermanent delegates and returns success")
    void deleteAllMerchantPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanentMerchants()).thenReturn(Future.succeededFuture());

        handler.deleteAllMerchantPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchants permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantPermanent delegates error when service fails")
    void deleteAllMerchantPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanentMerchants()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.deleteAllMerchantPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}