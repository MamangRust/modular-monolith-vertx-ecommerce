package io.example.order.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.service.OrderQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.order.OrderCommon.FindByIdOrderRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class OrderQueryHandlerTest {

    @Mock
    private OrderQueryService service;

    private OrderQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderQueryHandler(service);
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

    private static PagedResult<OrderResponse> aPagedResultOrder() {
        return new PagedResult<>(List.of(anOrderResponse()), 1);
    }

    private static PagedResult<OrderResponseDeleteAt> aPagedResultOrderDeleteAt() {
        return new PagedResult<>(List.of(anOrderResponseDeleteAt()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getAll(any())).thenReturn(Future.succeededFuture(aPagedResultOrder()));

        handler.findAll(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().setPage(1).setPageSize(10).build())
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

        handler.findAll(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(service.getById(anyLong())).thenReturn(Future.succeededFuture(anOrderResponse()));

        handler.findById(FindByIdOrderRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getById(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        handler.findById(FindByIdOrderRequest.newBuilder().setId(99).build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getActive(any())).thenReturn(Future.succeededFuture(aPagedResultOrder()));

        handler.findByActive(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    // Order aktif tidak punya deleted_at.
                    assertThat(resp.getData(0).hasDeletedAt()).isFalse();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates error when service fails")
    void findByActiveError(VertxTestContext ctx) {
        when(service.getActive(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByActive(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultOrderDeleteAt()));

        handler.findByTrashed(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().setPage(1).setPageSize(10).build())
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

        handler.findByTrashed(pb.order.OrderQuery.FindAllOrderRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}