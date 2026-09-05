package io.example.merchant_award.handler;

import com.google.protobuf.Empty;

import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.example.merchant_award.service.MerchantAwardCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant_award.MerchantAwardCommon.FindByIdMerchantAwardRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantAwardCommandHandlerTest {

    @Mock
    private MerchantAwardCommandService service;

    private MerchantAwardCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantAwardCommandHandler(service);
    }

    private static MerchantAwardResponse anAwardResponse() {
        return MerchantAwardResponse.builder()
                .id(1)
                .merchantId(10)
                .title("Best Merchant 2024")
                .description("Awarded for excellent service")
                .issuedBy("Government")
                .issueDate("2024-01-01")
                .expiryDate("2025-01-01")
                .certificateUrl("http://url.com/cert.pdf")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static MerchantAwardResponseDeleteAt anAwardResponseDeleteAt() {
        return MerchantAwardResponseDeleteAt.builder()
                .id(1)
                .merchantId(10)
                .title("Best Merchant 2024")
                .description("Awarded for excellent service")
                .issuedBy("Government")
                .issueDate("2024-01-01")
                .expiryDate("2025-01-01")
                .certificateUrl("http://url.com/cert.pdf")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(anAwardResponse()));

        var req = pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest.newBuilder()
                .setMerchantId(10)
                .setTitle("Best Merchant 2024")
                .setDescription("Awarded for excellent service")
                .setIssuedBy("Government")
                .setIssueDate("2024-01-01")
                .setExpiryDate("2025-01-01")
                .setCertificateUrl("http://url.com/cert.pdf")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getTitle()).isEqualTo("Best Merchant 2024");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(anAwardResponse()));

        var req = pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest.newBuilder()
                .setMerchantCertificationId(1)
                .setTitle("Updated Award Title")
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

        handler.update(pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantAward delegates and returns response")
    void trashedMerchantAward(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(anAwardResponseDeleteAt()));

        handler.trashedMerchantAward(FindByIdMerchantAwardRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantAward delegates error when service fails")
    void trashedMerchantAwardError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedMerchantAward(FindByIdMerchantAwardRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantAward delegates and returns response")
    void restoreMerchantAward(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(anAwardResponseDeleteAt()));

        handler.restoreMerchantAward(FindByIdMerchantAwardRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantAward delegates error when service fails")
    void restoreMerchantAwardError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreMerchantAward(FindByIdMerchantAwardRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantAwardPermanent delegates and returns success")
    void deleteMerchantAwardPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteMerchantAwardPermanent(FindByIdMerchantAwardRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Award deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantAwardPermanent delegates error when service fails")
    void deleteMerchantAwardPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteMerchantAwardPermanent(FindByIdMerchantAwardRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantAward delegates and returns success")
    void restoreAllMerchantAward(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllMerchantAward(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All awards restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantAward delegates error when service fails")
    void restoreAllMerchantAwardError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllMerchantAward(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantAwardPermanent delegates and returns success")
    void deleteAllMerchantAwardPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllMerchantAwardPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All awards permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantAwardPermanent delegates error when service fails")
    void deleteAllMerchantAwardPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllMerchantAwardPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}