package io.example.merchant_business.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.service.MerchantBusinessQueryService;
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
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantBusinessQueryHandlerTest {

    @Mock
    private MerchantBusinessQueryService service;

    private MerchantBusinessQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantBusinessQueryHandler(service);
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

    private static PagedResult<MerchantBusinessResponse> aPagedResultBusiness() {
        return new PagedResult<>(List.of(aBusinessResponse()), 1);
    }

    private static PagedResult<MerchantBusinessResponseDeleteAt> aPagedResultBusinessDeleteAt() {
        return new PagedResult<>(List.of(aBusinessResponseDeleteAt()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getAll(any())).thenReturn(Future.succeededFuture(aPagedResultBusiness()));

        var req = FindAllMerchantRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .setSearch("RETAIL")
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
        when(service.getAll(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findAll(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(service.getById(anyLong())).thenReturn(Future.succeededFuture(aBusinessResponse()));

        handler.findById(FindByIdMerchantBusinessRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getBusinessType()).isEqualTo("RETAIL");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getById(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        handler.findById(FindByIdMerchantBusinessRequest.newBuilder().setId(99).build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getActive(any())).thenReturn(Future.succeededFuture(aPagedResultBusinessDeleteAt()));

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
        when(service.getActive(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByActive(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultBusinessDeleteAt()));

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
        when(service.getTrashed(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByTrashed(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}