package io.example.order_item.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.service.OrderItemQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class OrderItemQueryHandlerTest {

    @Mock
    private OrderItemQueryService service;

    private OrderItemQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderItemQueryHandler(service);
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

    private static PagedResult<OrderItemResponse> aPagedResultOrderItem() {
        return new PagedResult<>(List.of(anOrderItemResponse()), 1);
    }

    private static PagedResult<OrderItemResponseDeleteAt> aPagedResultOrderItemDeleteAt() {
        return new PagedResult<>(List.of(anOrderItemResponseDeleteAt()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getAll(any())).thenReturn(Future.succeededFuture(aPagedResultOrderItem()));

        handler.findAll(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
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

        handler.findAll(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getActive(any())).thenReturn(Future.succeededFuture(aPagedResultOrderItemDeleteAt()));

        handler.findByActive(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setPage(1).setPageSize(10).build())
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

        handler.findByActive(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultOrderItemDeleteAt()));

        handler.findByTrashed(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setPage(1).setPageSize(10).build())
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

        handler.findByTrashed(pb.order_item.OrderItemQuery.FindAllOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findOrderItemByOrder delegates and returns list response")
    void findOrderItemByOrder(VertxTestContext ctx) {
        when(service.getByOrderId(anyLong())).thenReturn(Future.succeededFuture(List.of(anOrderItemResponse())));

        handler.findOrderItemByOrder(FindByIdOrderItemRequest.newBuilder().setId(100).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findOrderItemByOrder delegates error when service fails")
    void findOrderItemByOrderError(VertxTestContext ctx) {
        when(service.getByOrderId(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findOrderItemByOrder(FindByIdOrderItemRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}