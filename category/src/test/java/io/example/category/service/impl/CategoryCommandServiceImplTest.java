package io.example.category.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.example.category.domain.requests.CreateCategoryRequest;
import io.example.category.model.Category;
import io.example.category.repository.CategoryCommandRepository;
import io.example.common.exception.grpc.ConflictException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.grpc.Status.Code;
import io.opentelemetry.api.OpenTelemetry;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class CategoryCommandServiceImplTest {

    @Mock
    private CategoryCommandRepository repo;

    @Mock
    private RedisService redis;

    private CategoryCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryCommandServiceImpl(repo, redis,
                new TracingMetrics(OpenTelemetry.noop(), "category-test"));
    }

    private static CreateCategoryRequest aCreateRequest() {
        return CreateCategoryRequest.builder()
                .name("Technology")
                .description("Tech related items")
                .slugCategory("technology")
                .imageCategory("tech.png")
                .build();
    }

    private static Category aCategory() {
        Timestamp now = Timestamp.valueOf(LocalDateTime.of(2026, 6, 26, 10, 0, 0));
        return Category.builder()
                .categoryId(1L)
                .name("Technology")
                .description("Tech related items")
                .slugCategory("technology")
                .imageCategory("tech.png")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("create succeeds and maps to CategoryResponse")
    void createSuccess(VertxTestContext ctx) {
        when(repo.createCategory(any())).thenReturn(Future.succeededFuture(aCategory()));

        service.create(aCreateRequest())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getId()).isEqualTo(1L);
                    assertThat(resp.getName()).isEqualTo("Technology");
                    assertThat(resp.getSlugCategory()).isEqualTo("technology");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create maps PG unique violation 23505 to ConflictException")
    void createUniqueViolation23505(VertxTestContext ctx) {
        when(repo.createCategory(any())).thenReturn(Future.failedFuture(new RuntimeException(
                "ERROR: duplicate key value violates unique constraint \"uq_categories_active_slug\" (SQLSTATE 23505)")));

        service.create(aCreateRequest())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(ConflictException.class);
                    assertThat(err.getMessage())
                            .isEqualTo("Category with slug 'technology' already exists");
                    assertThat(((ConflictException) err).getGrpcStatusCode())
                            .isEqualTo(Code.ALREADY_EXISTS);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create maps generic duplicate key message to ConflictException")
    void createDuplicateKeyMessage(VertxTestContext ctx) {
        when(repo.createCategory(any())).thenReturn(Future.failedFuture(new RuntimeException(
                "duplicate key value violates unique constraint \"uq_categories_active_slug\"")));

        service.create(aCreateRequest())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(ConflictException.class);
                    assertThat(err.getMessage())
                            .isEqualTo("Category with slug 'technology' already exists");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create maps constraint name uq_categories_active_slug to ConflictException")
    void createUniqueConstraintName(VertxTestContext ctx) {
        when(repo.createCategory(any())).thenReturn(Future.failedFuture(new RuntimeException(
                "ERROR: new row for relation \"categories\" violates check constraint \"uq_categories_active_slug\"")));

        service.create(aCreateRequest())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(ConflictException.class);
                    assertThat(err.getMessage())
                            .isEqualTo("Category with slug 'technology' already exists");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create detects unique violation nested in the cause chain")
    void createUniqueViolationInCauseChain(VertxTestContext ctx) {
        Throwable pgViolation = new RuntimeException(
                "ERROR: duplicate key value violates unique constraint \"uq_categories_active_slug\" (SQLSTATE 23505)");
        when(repo.createCategory(any()))
                .thenReturn(Future.failedFuture(new RuntimeException("DB call failed", pgViolation)));

        service.create(aCreateRequest())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(ConflictException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create rethrows non-unique errors untouched")
    void createNonUniqueError(VertxTestContext ctx) {
        RuntimeException dbError = new RuntimeException("connection refused");
        when(repo.createCategory(any())).thenReturn(Future.failedFuture(dbError));

        service.create(aCreateRequest())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isSameAs(dbError);
                    assertThat(err).isNotInstanceOf(ConflictException.class);
                    ctx.completeNow();
                })));
    }
}
