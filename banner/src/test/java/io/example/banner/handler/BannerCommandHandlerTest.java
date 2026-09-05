package io.example.banner.handler;

import com.google.protobuf.Empty;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.service.BannerCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.banner.BannerCommon.FindByIdBannerRequest;
import pb.banner.BannerCommand.CreateBannerRequest;
import pb.banner.BannerCommand.UpdateBannerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class BannerCommandHandlerTest {

    @Mock
    private BannerCommandService service;

    private BannerCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BannerCommandHandler(service);
    }

    private static BannerResponse aBannerResponse() {
        return BannerResponse.builder()
                .id(1L)
                .name("Summer Sale")
                .startDate("2024-01-01")
                .endDate("2024-12-31")
                .startTime("00:00")
                .endTime("23:59")
                .isActive(true)
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .build();
    }

    private static BannerResponseDeleteAt aBannerResponseDeleteAt() {
        return BannerResponseDeleteAt.builder()
                .id(1L)
                .name("Summer Sale")
                .startDate("2024-01-01")
                .endDate("2024-12-31")
                .startTime("00:00")
                .endTime("23:59")
                .isActive(true)
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .deletedAt("2026-06-25T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        // ✅ Diubah: Mock langsung return object domain, bukan CmdResp
        when(service.createBanner(any())).thenReturn(Future.succeededFuture(aBannerResponse()));

        var req = CreateBannerRequest.newBuilder()
                .setName("Summer Sale")
                .setStartDate("2024-01-01")
                .setEndDate("2024-12-31")
                .setStartTime("00:00")
                .setEndTime("23:59")
                .setIsActive(true)
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    // ✅ Assertion mengikuti hardcode dari Handler ("success", "OK")
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getBannerId()).isEqualTo(1);
                    assertThat(resp.getData().getName()).isEqualTo("Summer Sale");
                    ctx.completeNow();
                })));
    }

    /* ─── update ─── */

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        // ✅ Diubah: Mock langsung return object domain
        when(service.updateBanner(any())).thenReturn(Future.succeededFuture(aBannerResponse()));

        var req = UpdateBannerRequest.newBuilder()
                .setBannerId(1)
                .setName("Summer Sale")
                .setStartDate("2024-01-01")
                .setEndDate("2024-12-31")
                .setStartTime("00:00")
                .setEndTime("23:59")
                .setIsActive(true)
                .build();

        handler.update(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getBannerId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

 
    @Test
    @DisplayName("trash delegates and returns delete-at response")
    void trash(VertxTestContext ctx) {
        // ✅ Diubah: Mock langsung return object domain
        when(service.trashBanner(anyLong())).thenReturn(Future.succeededFuture(aBannerResponseDeleteAt()));

        var req = FindByIdBannerRequest.newBuilder().setId(1).build();

        handler.trash(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getBannerId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    /* ─── restore ─── */

    @Test
    @DisplayName("restore delegates and returns delete-at response")
    void restore(VertxTestContext ctx) {
        // ✅ Diubah: Mock langsung return object domain
        when(service.restoreBanner(anyLong())).thenReturn(Future.succeededFuture(aBannerResponseDeleteAt()));

        var req = FindByIdBannerRequest.newBuilder().setId(1).build();

        handler.restore(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getBannerId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    /* ─── deletePermanent ─── */

    @Test
    @DisplayName("deletePermanent delegates and returns success")
    void deletePermanent(VertxTestContext ctx) {
        // ✅ Diubah: Return Future.succeededFuture(null) karena service return Future<Void>
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        var req = FindByIdBannerRequest.newBuilder().setId(1).build();

        handler.deletePermanent(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Banner deleted permanently");
                    ctx.completeNow();
                })));
    }

    /* ─── restoreAll ─── */

    @Test
    @DisplayName("restoreAll delegates and returns success")
    void restoreAll(VertxTestContext ctx) {
        // ✅ Diubah: Return Future.succeededFuture(null)
        when(service.restoreAllBanners()).thenReturn(Future.succeededFuture());

        handler.restoreAll(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All banners restored successfully");
                    ctx.completeNow();
                })));
    }

    /* ─── deleteAll ─── */

    @Test
    @DisplayName("deleteAll delegates and returns success")
    void deleteAll(VertxTestContext ctx) {
        // ✅ Diubah: Return Future.succeededFuture(null)
        when(service.deleteAllPermanentBanners()).thenReturn(Future.succeededFuture());

        handler.deleteAll(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All banners permanently deleted");
                    ctx.completeNow();
                })));
    }

    /* ─── error path ─── */

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.createBanner(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CreateBannerRequest.newBuilder().setName("Test Banner").build();

        // ✅ Handler menggunakan GrpcExceptionMapper: error tak dikenal dibungkus
        // StatusRuntimeException dengan status INTERNAL (bukan StatusException).
        handler.create(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    io.grpc.StatusRuntimeException sre = (io.grpc.StatusRuntimeException) err;
                    assertThat(sre.getStatus().getCode()).isEqualTo(io.grpc.Status.Code.INTERNAL);
                    ctx.completeNow();
                })));
    }
}