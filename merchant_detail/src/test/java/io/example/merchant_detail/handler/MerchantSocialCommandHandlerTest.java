package io.example.merchant_detail.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class MerchantSocialCommandHandlerTest {

    @Mock
    private MerchantSocialLinkCommandService service;

    private MerchantSocialCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MerchantSocialCommandHandler(service);
    }

    private static MerchantSocialMediaLinkResponse aSocialResponse() {
        return MerchantSocialMediaLinkResponse.builder()
                .id(1L)
                .merchantDetailId(10)
                .platform("INSTAGRAM")
                .url("https://instagram.com/myshop")
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(aSocialResponse()));

        var req = CreateMerchantSocialRequest.newBuilder()
                .setMerchantDetailId(10)
                .setPlatform("INSTAGRAM")
                .setUrl("https://instagram.com/myshop")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getPlatform()).isEqualTo("INSTAGRAM");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(CreateMerchantSocialRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(aSocialResponse()));

        var req = UpdateMerchantSocialRequest.newBuilder()
                .setId(1)
                .setMerchantDetailId(10)
                .setPlatform("TIKTOK")
                .setUrl("https://tiktok.com/@myshop")
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

        handler.update(UpdateMerchantSocialRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}