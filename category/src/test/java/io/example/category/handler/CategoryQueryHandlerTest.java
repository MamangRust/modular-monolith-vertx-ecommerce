package io.example.category.handler;

import java.util.List;

import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.service.CategoryQueryService;
import io.example.common.domain.PagedResult;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.category.CategoryCommon;
import pb.category.CategoryQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class CategoryQueryHandlerTest {

    @Mock
    private CategoryQueryService queryService;

    private CategoryQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CategoryQueryHandler(queryService);
    }

    private static CategoryResponse aCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("Technology")
                .description("Tech related items")
                .slugCategory("technology")
                .imageCategory("tech.png")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .build();
    }

    private static CategoryResponseDeleteAt aCategoryResponseDeleteAt() {
        return CategoryResponseDeleteAt.builder()
                .id(1L)
                .name("Technology")
                .description("Tech related items")
                .slugCategory("technology")
                .imageCategory("tech.png")
                .createdAt("2026-06-26T10:00:00Z")
                .updatedAt("2026-06-26T10:00:00Z")
                .deletedAt("2026-06-25T10:00:00Z")
                .build();
    }

    private static PagedResult<CategoryResponse> aPagedResultCategory() {
        return new PagedResult<>(List.of(aCategoryResponse()), 1);
    }

    private static PagedResult<CategoryResponseDeleteAt> aPagedResultCategoryDeleteAt() {
        return new PagedResult<>(List.of(aCategoryResponseDeleteAt()), 1);
    }

    /* ─── findAll ─── */

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(queryService.getAll(any())).thenReturn(Future.succeededFuture(aPagedResultCategory()));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .setSearch("tech")
                .build();

        handler.findAll(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getData(0).getName()).isEqualTo("Technology");
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    assertThat(resp.getPagination().getCurrentPage()).isEqualTo(1);
                    assertThat(resp.getPagination().getPageSize()).isEqualTo(10);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAll delegates error when service fails")
    void findAllError(VertxTestContext ctx) {
        when(queryService.getAll(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder().build();

        handler.findAll(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    /* ─── findById ─── */

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(queryService.getById(anyLong())).thenReturn(Future.succeededFuture(aCategoryResponse()));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.findById(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getName()).isEqualTo("Technology");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(queryService.getById(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(99).build();

        handler.findById(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }


    @Test
    @DisplayName("findByActive delegates and returns paginated delete-at response")
    void findByActive(VertxTestContext ctx) {
        when(queryService.getActive(any())).thenReturn(Future.succeededFuture(aPagedResultCategoryDeleteAt()));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .build();

        handler.findByActive(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getData(0).hasDeletedAt()).isTrue();
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates error when service fails")
    void findByActiveError(VertxTestContext ctx) {
        when(queryService.getActive(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder().build();

        handler.findByActive(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    /* ─── findByTrashed ─── */

    @Test
    @DisplayName("findByTrashed delegates and returns paginated delete-at response")
    void findByTrashed(VertxTestContext ctx) {
        when(queryService.getTrashed(any())).thenReturn(Future.succeededFuture(aPagedResultCategoryDeleteAt()));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setPage(1)
                .setPageSize(10)
                .build();

        handler.findByTrashed(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getData(0).hasDeletedAt()).isTrue();
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates error when service fails")
    void findByTrashedError(VertxTestContext ctx) {
        when(queryService.getTrashed(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryQuery.FindAllCategoryRequest.newBuilder().build();

        handler.findByTrashed(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}