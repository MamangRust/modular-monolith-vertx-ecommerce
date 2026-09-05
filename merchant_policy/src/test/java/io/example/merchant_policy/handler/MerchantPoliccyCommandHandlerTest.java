package io.example.merchant_policy.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.Empty;

import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;
import io.example.merchant_policy.service.MerchantPoliciesCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommon.FindByIdMerchantPoliciesRequest;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantPolicyCommandHandlerTest {

    @Mock
    private MerchantPoliciesCommandService service;

    private MerchantPolicyCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantPolicyCommandHandler(service);
    }

    private static MerchantPoliciesResponse aPolicyResponse() {
        return MerchantPoliciesResponse.builder()
                .id(1L)
                .merchantId(10)
                .policyType("RETURN_POLICY")
                .title("Return Policy 30 Days")
                .description("Items can be returned within 30 days")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static MerchantPoliciesResponseDeleteAt aPolicyResponseDeleteAt() {
        return MerchantPoliciesResponseDeleteAt.builder()
                .id(1L)
                .merchantId(10)
                .policyType("RETURN_POLICY")
                .title("Return Policy 30 Days")
                .description("Items can be returned within 30 days")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-01-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(aPolicyResponse()));

        var req = CreateMerchantPoliciesRequest.newBuilder()
                .setMerchantId(10)
                .setPolicyType("RETURN_POLICY")
                .setTitle("Return Policy 30 Days")
                .setDescription("Items can be returned within 30 days")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getTitle()).isEqualTo("Return Policy 30 Days");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(CreateMerchantPoliciesRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(aPolicyResponse()));

        var req = UpdateMerchantPoliciesRequest.newBuilder()
                .setMerchantPolicyId(1)
                .setTitle("Updated Policy Title")
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

        handler.update(UpdateMerchantPoliciesRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantPolicies delegates and returns response")
    void trashedMerchantPolicies(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(aPolicyResponseDeleteAt()));

        handler.trashedMerchantPolicies(FindByIdMerchantPoliciesRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedMerchantPolicies delegates error when service fails")
    void trashedMerchantPoliciesError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedMerchantPolicies(FindByIdMerchantPoliciesRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantPolicies delegates and returns response")
    void restoreMerchantPolicies(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(aPolicyResponseDeleteAt()));

        handler.restoreMerchantPolicies(FindByIdMerchantPoliciesRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreMerchantPolicies delegates error when service fails")
    void restoreMerchantPoliciesError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreMerchantPolicies(FindByIdMerchantPoliciesRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantPoliciesPermanent delegates and returns success")
    void deleteMerchantPoliciesPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteMerchantPoliciesPermanent(FindByIdMerchantPoliciesRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Policy deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteMerchantPoliciesPermanent delegates error when service fails")
    void deleteMerchantPoliciesPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteMerchantPoliciesPermanent(FindByIdMerchantPoliciesRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantPolicies delegates and returns success")
    void restoreAllMerchantPolicies(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllMerchantPolicies(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All policies restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllMerchantPolicies delegates error when service fails")
    void restoreAllMerchantPoliciesError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllMerchantPolicies(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantPoliciesPermanent delegates and returns success")
    void deleteAllMerchantPoliciesPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllMerchantPoliciesPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All policies permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllMerchantPoliciesPermanent delegates error when service fails")
    void deleteAllMerchantPoliciesPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllMerchantPoliciesPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}