package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.service.MerchantDocumentCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant_document.MerchantDocumentCommand.CreateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.DeleteMerchantDocumentPermanentRequest;
import pb.merchant_document.MerchantDocumentCommand.RestoreMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.TrashedMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentStatusRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantDocumentCommandHandlerTest {

    @Mock
    private MerchantDocumentCommandService service;

    private MerchantDocumentCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantDocumentCommandHandler(service);
    }

    private static MerchantDocumentResponse aDocumentResponse() {
        return MerchantDocumentResponse.builder()
                .id(1)
                .merchantId(10)
                .documentType("KTP")
                .documentUrl("http://url.com/ktp.png")
                .status("APPROVED")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.createDocument(any())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        var req = CreateMerchantDocumentRequest.newBuilder()
                .setMerchantId(10)
                .setDocumentType("KTP")
                .setDocumentUrl("http://url.com/ktp.png")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getDocumentId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.createDocument(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.create(CreateMerchantDocumentRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.updateDocument(any())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        handler.update(UpdateMerchantDocumentRequest.newBuilder().setDocumentId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getDocumentId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates error when service fails")
    void updateError(VertxTestContext ctx) {
        when(service.updateDocument(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.update(UpdateMerchantDocumentRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateStatus delegates and returns response")
    void updateStatus(VertxTestContext ctx) {
        when(service.updateStatus(any())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        handler.updateStatus(UpdateMerchantDocumentStatusRequest.newBuilder().setDocumentId(1).setStatus("REJECTED").build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateStatus delegates error when service fails")
    void updateStatusError(VertxTestContext ctx) {
        when(service.updateStatus(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.updateStatus(UpdateMerchantDocumentStatusRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashed delegates and returns response")
    void trashed(VertxTestContext ctx) {
        when(service.trashDocument(anyLong())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        handler.trashed(TrashedMerchantDocumentRequest.newBuilder().setDocumentId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getDocumentId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashed delegates error when service fails")
    void trashedError(VertxTestContext ctx) {
        when(service.trashDocument(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.trashed(TrashedMerchantDocumentRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restore delegates and returns response")
    void restore(VertxTestContext ctx) {
        when(service.restoreDocument(anyLong())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        handler.restore(RestoreMerchantDocumentRequest.newBuilder().setDocumentId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restore delegates error when service fails")
    void restoreError(VertxTestContext ctx) {
        when(service.restoreDocument(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.restore(RestoreMerchantDocumentRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deletePermanent delegates and returns success")
    void deletePermanent(VertxTestContext ctx) {
        when(service.deleteDocumentPermanently(anyLong())).thenReturn(Future.succeededFuture());

        handler.deletePermanent(DeleteMerchantDocumentPermanentRequest.newBuilder().setDocumentId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Document deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deletePermanent delegates error when service fails")
    void deletePermanentError(VertxTestContext ctx) {
        when(service.deleteDocumentPermanently(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.deletePermanent(DeleteMerchantDocumentPermanentRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAll delegates and returns success")
    void restoreAll(VertxTestContext ctx) {
        when(service.restoreAllDocuments()).thenReturn(Future.succeededFuture());

        handler.restoreAll(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All documents restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAll delegates error when service fails")
    void restoreAllError(VertxTestContext ctx) {
        when(service.restoreAllDocuments()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.restoreAll(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllPermanent delegates and returns success")
    void deleteAllPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanentDocuments()).thenReturn(Future.succeededFuture());

        handler.deleteAllPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All documents permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllPermanent delegates error when service fails")
    void deleteAllPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanentDocuments()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.deleteAllPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}