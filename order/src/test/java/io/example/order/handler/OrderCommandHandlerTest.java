package io.example.order.handler;

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

import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.service.OrderCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import pb.order.OrderCommand.CreateOrderItemRequest;
import pb.order.OrderCommand.CreateOrderRequest;
import pb.order.OrderCommand.UpdateOrderItemRequest;
import pb.order.OrderCommand.UpdateOrderRequest;
import pb.order.OrderCommon.FindByIdOrderRequest;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class OrderCommandHandlerTest {

    @Mock
    private OrderCommandService service;

    private OrderCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderCommandHandler(service);
    }

    private static OrderResponse anOrderResponse() {
        return OrderResponse.builder()
                .id(1L)
                .userId(10)
                .merchantId(20)
                .totalPrice(150000)
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static OrderResponseDeleteAt anOrderResponseDeleteAt() {
        return OrderResponseDeleteAt.builder()
                .id(1L)
                .userId(10)
                .merchantId(20)
                .totalPrice(150000)
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    private CreateOrderRequest buildCreateProto() {
        return CreateOrderRequest.newBuilder()
                .setMerchantId(20)
                .setUserId(10)
                .addItems(CreateOrderItemRequest.newBuilder().setProductId(1).setQuantity(2).build())
                .setShipping(pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest.newBuilder()
                        .setAlamat("Jl. Test")
                        .setKota("Jakarta")
                        .setProvinsi("DKI")
                        .setNegara("Indonesia")
                        .setCourier("JNE")
                        .setShippingMethod("REG")
                        .setShippingCost(10000)
                        .build())
                .build();
    }

    private UpdateOrderRequest buildUpdateProto() {
        return UpdateOrderRequest.newBuilder()
                .setOrderId(1)
                .setUserId(10)
                .addItems(UpdateOrderItemRequest.newBuilder().setOrderItemId(1).setProductId(1).setQuantity(3).build())
                .setShipping(pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest.newBuilder()
                        .setShippingId(1)
                        .setOrderId(1)
                        .setAlamat("Jl. Test Updated")
                        .setKota("Bandung")
                        .setProvinsi("Jabar")
                        .setNegara("Indonesia")
                        .setCourier("JNE")
                        .setShippingMethod("YES")
                        .setShippingCost(15000)
                        .build())
                .build();
    }

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.createOrder(any())).thenReturn(Future.succeededFuture(anOrderResponse()));

        handler.create(buildCreateProto())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.createOrder(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(buildCreateProto())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.updateOrder(any())).thenReturn(Future.succeededFuture(anOrderResponse()));

        handler.update(buildUpdateProto())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates error when service fails")
    void updateError(VertxTestContext ctx) {
        when(service.updateOrder(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.update(buildUpdateProto())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedOrder delegates and returns response")
    void trashedOrder(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(anOrderResponseDeleteAt()));

        handler.trashedOrder(FindByIdOrderRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedOrder delegates error when service fails")
    void trashedOrderError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedOrder(FindByIdOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreOrder delegates and returns response")
    void restoreOrder(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(anOrderResponseDeleteAt()));

        handler.restoreOrder(FindByIdOrderRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreOrder delegates error when service fails")
    void restoreOrderError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreOrder(FindByIdOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderPermanent delegates and returns success")
    void deleteOrderPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteOrderPermanent(FindByIdOrderRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Order deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderPermanent delegates error when service fails")
    void deleteOrderPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteOrderPermanent(FindByIdOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllOrder delegates and returns success")
    void restoreAllOrder(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllOrder(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All orders restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllOrder delegates error when service fails")
    void restoreAllOrderError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllOrder(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllOrderPermanent delegates and returns success")
    void deleteAllOrderPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllOrderPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All orders permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllOrderPermanent delegates error when service fails")
    void deleteAllOrderPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllOrderPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}