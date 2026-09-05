package io.example.order_item.handler;

import java.util.List;

import com.google.protobuf.Empty;

import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.service.OrderItemCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.order_item.OrderItemCommand.CalculateTotalPriceRequest;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class OrderItemCommandHandlerTest {

    @Mock
    private OrderItemCommandService service;

    private OrderItemCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderItemCommandHandler(service);
    }

    private static OrderItemResponse anOrderItemResponse() {
        return OrderItemResponse.builder()
                .id(1L)
                .orderId(100)
                .productId(50)
                .quantity(2)
                .price(25000)
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static OrderItemResponseDeleteAt anOrderItemResponseDeleteAt() {
        return OrderItemResponseDeleteAt.builder()
                .id(1L)
                .orderId(100)
                .productId(50)
                .quantity(2)
                .price(25000)
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    @Test
    @DisplayName("createOrderItem delegates and returns response")
    void createOrderItem(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(anOrderItemResponse()));

        var req = pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest.newBuilder()
                .setOrderId(100)
                .setProductId(50)
                .setQuantity(2)
                .setPrice(25000)
                .build();

        handler.createOrderItem(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("createOrderItem delegates error when service fails")
    void createOrderItemError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.createOrderItem(pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateOrderItem delegates and returns response")
    void updateOrderItem(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(anOrderItemResponse()));

        var req = pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest.newBuilder()
                .setOrderItemId(1)
                .setQuantity(3)
                .setPrice(25000)
                .build();

        handler.updateOrderItem(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("updateOrderItem delegates error when service fails")
    void updateOrderItemError(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.updateOrderItem(pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashOrderItem delegates and returns response")
    void trashOrderItem(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(List.of(anOrderItemResponseDeleteAt())));

        handler.trashOrderItem(FindByIdOrderItemRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashOrderItem delegates error when service fails")
    void trashOrderItemError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashOrderItem(FindByIdOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreOrderItem delegates and returns response")
    void restoreOrderItem(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(List.of(anOrderItemResponseDeleteAt())));

        handler.restoreOrderItem(FindByIdOrderItemRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreOrderItem delegates error when service fails")
    void restoreOrderItemError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreOrderItem(FindByIdOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderItemPermanent delegates and returns success")
    void deleteOrderItemPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteOrderItemPermanent(FindByIdOrderItemRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Order item deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderItemPermanent delegates error when service fails")
    void deleteOrderItemPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteOrderItemPermanent(FindByIdOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllOrdersItem delegates and returns success")
    void restoreAllOrdersItem(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllOrdersItem(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All order items restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllOrdersItem delegates error when service fails")
    void restoreAllOrdersItemError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllOrdersItem(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllPermanentOrdersItem delegates and returns success")
    void deleteAllPermanentOrdersItem(VertxTestContext ctx) {
        when(service.deleteAll()).thenReturn(Future.succeededFuture());

        handler.deleteAllPermanentOrdersItem(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All order items permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllPermanentOrdersItem delegates error when service fails")
    void deleteAllPermanentOrdersItemError(VertxTestContext ctx) {
        when(service.deleteAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllPermanentOrdersItem(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderItemByOrderPermanent delegates and returns success")
    void deleteOrderItemByOrderPermanent(VertxTestContext ctx) {
        when(service.deleteByOrderPermanent(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteOrderItemByOrderPermanent(FindByIdOrderItemRequest.newBuilder().setId(100).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Order items deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteOrderItemByOrderPermanent delegates error when service fails")
    void deleteOrderItemByOrderPermanentError(VertxTestContext ctx) {
        when(service.deleteByOrderPermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteOrderItemByOrderPermanent(FindByIdOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("calculateTotalPrice delegates and returns response")
    void calculateTotalPrice(VertxTestContext ctx) {
        when(service.calculateTotalPrice(anyLong())).thenReturn(Future.succeededFuture(java.math.BigDecimal.valueOf(50000.00)));

        handler.calculateTotalPrice(CalculateTotalPriceRequest.newBuilder().setOrderId(100).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getTotalPrice()).isEqualTo(50000);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("calculateTotalPrice delegates error when service fails")
    void calculateTotalPriceError(VertxTestContext ctx) {
        when(service.calculateTotalPrice(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.calculateTotalPrice(CalculateTotalPriceRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}