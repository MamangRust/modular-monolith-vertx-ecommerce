package io.example.merchant.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.example.merchant.service.MerchantDocumentQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest;
import pb.merchant_document.MerchantDocumentQuery.FindMerchantDocumentByIdRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantDocumentQueryHandlerTest {

    @Mock
    private MerchantDocumentQueryService service;

    private MerchantDocumentQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantDocumentQueryHandler(service);
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

    private static MerchantDocumentResponseDeleteAt aDocumentResponseDeleteAt() {
        return MerchantDocumentResponseDeleteAt.builder()
                .id(1)
                .merchantId(10)
                .documentType("KTP")
                .documentUrl("http://url.com/ktp.png")
                .status("APPROVED")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .deletedAt("2026-06-25T10:00:00Z")
                .build();
    }

    private static PagedResult<MerchantDocumentResponse> aPagedResultDoc() {
        return new PagedResult<>(List.of(aDocumentResponse()), 1);
    }

    private static PagedResult<MerchantDocumentResponseDeleteAt> aPagedResultDocDeleteAt() {
        return new PagedResult<>(List.of(aDocumentResponseDeleteAt()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getAllDocuments(any())).thenReturn(Future.succeededFuture(aPagedResultDoc()));

        handler.findAll(FindAllMerchantDocumentsRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getDocumentId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAll delegates error when service fails")
    void findAllError(VertxTestContext ctx) {
        when(service.getAllDocuments(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.findAll(FindAllMerchantDocumentsRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAllActive delegates and returns paginated response")
    void findAllActive(VertxTestContext ctx) {
        when(service.getActiveDocuments(any())).thenReturn(Future.succeededFuture(aPagedResultDoc()));

        handler.findAllActive(FindAllMerchantDocumentsRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAllActive delegates error when service fails")
    void findAllActiveError(VertxTestContext ctx) {
        when(service.getActiveDocuments(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.findAllActive(FindAllMerchantDocumentsRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAllTrashed delegates and returns paginated response")
    void findAllTrashed(VertxTestContext ctx) {
        when(service.getTrashedDocuments(any())).thenReturn(Future.succeededFuture(aPagedResultDocDeleteAt()));

        handler.findAllTrashed(FindAllMerchantDocumentsRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAllTrashed delegates error when service fails")
    void findAllTrashedError(VertxTestContext ctx) {
        when(service.getTrashedDocuments(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));
        handler.findAllTrashed(FindAllMerchantDocumentsRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(service.getDocumentById(anyLong())).thenReturn(Future.succeededFuture(aDocumentResponse()));

        handler.findById(FindMerchantDocumentByIdRequest.newBuilder().setDocumentId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getDocumentId()).isEqualTo(1);
                    assertThat(resp.getData().getDocumentType()).isEqualTo("KTP");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getDocumentById(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));
        handler.findById(FindMerchantDocumentByIdRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}