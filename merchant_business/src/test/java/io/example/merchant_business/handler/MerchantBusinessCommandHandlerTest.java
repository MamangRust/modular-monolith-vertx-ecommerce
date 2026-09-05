package io.example.merchant_business.handler;

import com.google.protobuf.Empty;

import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.service.MerchantBusinessCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantBusinessCommandHandlerTest {

    @Mock
    private MerchantBusinessCommandService service;

    private MerchantBusinessCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantBusinessCommandHandler(service);
    }

    private static MerchantBusinessResponse aBusinessResponse() {
        return MerchantBusinessResponse.builder()
                .id(1)
                .merchantId(10)
                .businessType("RETAIL")
                .taxId("TAX-001")
                .establishedYear(2020)
                .numberOfEmployees(50)
                .websiteUrl("https://mybusiness.com")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static MerchantBusinessResponseDeleteAt aBusinessResponseDeleteAt() {
        return MerchantBusinessResponseDeleteAt.builder()
                .id(1)
                .merchantId(10)
                .businessType("RETAIL")
                .taxId("TAX-001")
                .establishedYear(2020)
                .numberOfEmployees(50)
                .websiteUrl("https://mybusiness.com")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(aBusinessResponse()));

        var req = CreateMerchantBusinessRequest.newBuilder()
                .setMerchantId(10)
                .setBusinessType("RETAIL")
                .setTaxId("TAX-001")
                .setEstablishedYear(2020)
                .setNumberOfEmployees(50)
                .setWebsiteUrl("https://mybusiness.com")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getBusinessType()).isEqualTo("RETAIL");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(CreateMerchantBusinessRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(aBusinessResponse()));

        var req = UpdateMerchantBusinessRequest.newBuilder()
                .setMerchantBusinessInfoId(1)
                .setBusinessType("WHOLESALE")
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

        handler.update(UpdateMerchantBusinessRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantBusiness delegates and returns response")
    void trashedMerchantBusiness(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(aBusinessResponseDeleteAt()));

        handler.trashedMerchantBusiness(FindByIdMerchantBusinessRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantBusiness delegates error when service fails")
    void trashedMerchantBusinessError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedMerchantBusiness(FindByIdMerchantBusinessRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantBusiness delegates and returns response")
    void restoreMerchantBusiness(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(aBusinessResponseDeleteAt()));

        handler.restoreMerchantBusiness(FindByIdMerchantBusinessRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantBusiness delegates error when service fails")
    void restoreMerchantBusinessError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreMerchantBusiness(FindByIdMerchantBusinessRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantBusinessPermanent delegates and returns success")
    void deleteMerchantBusinessPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteMerchantBusinessPermanent(FindByIdMerchantBusinessRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Merchant business info deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantBusinessPermanent delegates error when service fails")
    void deleteMerchantBusinessPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteMerchantBusinessPermanent(FindByIdMerchantBusinessRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantBusiness delegates and returns success")
    void restoreAllMerchantBusiness(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllMerchantBusiness(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchant business info restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantBusiness delegates error when service fails")
    void restoreAllMerchantBusinessError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllMerchantBusiness(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantBusinessPermanent delegates and returns success")
    void deleteAllMerchantBusinessPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllMerchantBusinessPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All merchant business info permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantBusinessPermanent delegates error when service fails")
    void deleteAllMerchantBusinessPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllMerchantBusinessPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}