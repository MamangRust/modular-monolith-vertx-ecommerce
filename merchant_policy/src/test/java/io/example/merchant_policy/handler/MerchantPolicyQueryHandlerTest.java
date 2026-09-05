package io.example.merchant_policy.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.example.common.domain.PagedResult;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponse;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponseDeleteAt;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.service.MerchantPoliciesQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_policy.MerchantPolicyCommon.FindByIdMerchantPoliciesRequest;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantPolicyQueryHandlerTest {

    @Mock
    private MerchantPoliciesQueryService service;

    private MerchantPolicyQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantPolicyQueryHandler(service);
    }

    private static MerchantPoliciesRelationResponse aPolicyRelationResponse() {
        return MerchantPoliciesRelationResponse.builder()
                .id(1L)
                .merchantId(10)
                .policyType("RETURN_POLICY")
                .title("Return Policy 30 Days")
                .description("Items can be returned within 30 days")
                .merchantName("hello")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static MerchantPoliciesRelationResponseDeleteAt aPolicyRelationResponseDeleteAt() {
        return MerchantPoliciesRelationResponseDeleteAt.builder()
                .id(1L)
                .merchantId(10)
                .policyType("RETURN_POLICY")
                .title("Return Policy 30 Days")
                .description("Items can be returned within 30 days")
                .merchantName("hello")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static PagedResult<MerchantPoliciesRelationResponse> aPagedResultPolicy() {
        return new PagedResult<>(List.of(aPolicyRelationResponse()), 1);
    }

    private static PagedResult<MerchantPoliciesRelationResponseDeleteAt> aPagedResultPolicyDeleteAt(){
      return new PagedResult<>(List.of(aPolicyRelationResponseDeleteAt()), 1);
    }


    private static MerchantPoliciesResponse aPolicyResponseSingle() {
        return MerchantPoliciesResponse.builder()
                .id(1L)
                .merchantId(10)
                .policyType("RETURN_POLICY")
                .title("Return Policy 30 Days")
                .description("Items can be returned within 30 days")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .merchantName("Toko Sejahtera")
                .build();
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getMerchantPolicies(any())).thenReturn(Future.succeededFuture(aPagedResultPolicy()));

        var req = FindAllMerchantRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .setSearch("Return")
                .build();

        handler.findAll(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Data fetched successfully");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAll delegates error when service fails")
    void findAllError(VertxTestContext ctx) {
        when(service.getMerchantPolicies(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findAll(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        // ✅ Diperbaiki: Menggunakan aPolicyResponseSingle()
        when(service.getMerchantPolicy(anyLong())).thenReturn(Future.succeededFuture(aPolicyResponseSingle()));

        handler.findById(FindByIdMerchantPoliciesRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Data fetched successfully");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getTitle()).isEqualTo("Return Policy 30 Days");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getMerchantPolicy(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        handler.findById(FindByIdMerchantPoliciesRequest.newBuilder().setId(99).build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getMerchantPoliciesActive(any())).thenReturn(Future.succeededFuture(aPagedResultPolicyDeleteAt()));

        handler.findByActive(FindAllMerchantRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates error when service fails")
    void findByActiveError(VertxTestContext ctx) {
        when(service.getMerchantPoliciesActive(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByActive(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getMerchantPoliciesTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultPolicyDeleteAt()));

        handler.findByTrashed(FindAllMerchantRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates error when service fails")
    void findByTrashedError(VertxTestContext ctx) {
        when(service.getMerchantPoliciesTrashed(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByTrashed(FindAllMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}