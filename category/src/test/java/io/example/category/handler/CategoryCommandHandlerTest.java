package io.example.category.handler;

import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.service.CategoryCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.Empty;

import pb.category.CategoryCommon;
import pb.category.CategoryCommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class CategoryCommandHandlerTest {

    @Mock
    private CategoryCommandService service;

        private CategoryCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CategoryCommandHandler(service);
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


    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.succeededFuture(aCategoryResponse()));

        var req = CategoryCommand.CreateCategoryRequest.newBuilder()
                .setName("Technology")
                .setDescription("Tech related items")
                .setSlugCategory("technology")
                .setImageCategory("tech.png")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getName()).isEqualTo("Technology");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.create(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommand.CreateCategoryRequest.newBuilder().setName("Tech").build();

        handler.create(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.succeededFuture(aCategoryResponse()));

        var req = CategoryCommand.UpdateCategoryRequest.newBuilder()
                .setCategoryId(1)
                .setName("Technology Updated")
                .setDescription("Updated description")
                .setSlugCategory("technology-updated")
                .setImageCategory("tech-updated.png")
                .build();

        handler.update(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates error when service fails")
    void updateError(VertxTestContext ctx) {
        when(service.update(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommand.UpdateCategoryRequest.newBuilder()
                .setCategoryId(1)
                .setName("Tech")
                .build();

        handler.update(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedCategory delegates and returns delete-at response")
    void trashedCategory(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.succeededFuture(aCategoryResponseDeleteAt()));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.trashedCategory(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedCategory delegates error when service fails")
    void trashedCategoryError(VertxTestContext ctx) {
        when(service.trash(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.trashedCategory(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }


    @Test
    @DisplayName("restoreCategory delegates and returns delete-at response")
    void restoreCategory(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.succeededFuture(aCategoryResponseDeleteAt()));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.restoreCategory(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreCategory delegates error when service fails")
    void restoreCategoryError(VertxTestContext ctx) {
        when(service.restore(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.restoreCategory(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }


    @Test
    @DisplayName("deleteCategoryPermanent delegates and returns success")
    void deleteCategoryPermanent(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.succeededFuture());

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.deleteCategoryPermanent(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteCategoryPermanent delegates error when service fails")
    void deleteCategoryPermanentError(VertxTestContext ctx) {
        when(service.deletePermanent(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(1).build();

        handler.deleteCategoryPermanent(req)
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }


    @Test
    @DisplayName("restoreAllCategory delegates and returns success")
    void restoreAllCategory(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.succeededFuture());

        handler.restoreAllCategory(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllCategory delegates error when service fails")
    void restoreAllCategoryError(VertxTestContext ctx) {
        when(service.restoreAll()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllCategory(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }


    @Test
    @DisplayName("deleteAllCategoryPermanent delegates and returns success")
    void deleteAllCategoryPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.succeededFuture());

        handler.deleteAllCategoryPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllCategoryPermanent delegates error when service fails")
    void deleteAllCategoryPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanent()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllCategoryPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}