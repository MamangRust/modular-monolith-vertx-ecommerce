package io.example.review.handler;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.ReviewRelationsDetailResponse;
import io.example.review.model.ReviewDetailResponse;
import io.example.review.service.ReviewQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.review.ReviewQuery.FindAllReviewMerchantRequest;
import pb.review.ReviewQuery.FindAllReviewProductRequest;
import pb.review.ReviewQuery.FindAllReviewRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class ReviewQueryHandlerTest {

    @Mock
    private ReviewQueryService service;

    private ReviewQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReviewQueryHandler(service);
    }

    private static ReviewResponse aReviewResponse() {
        return ReviewResponse.builder()
                .id(1)
                .userId(10)
                .productId(50)
                .name("John Doe")
                .rating(5)
                .comment("Excellent product!")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static ReviewResponseDeleteAt aReviewResponseDeleteAt() {
        return ReviewResponseDeleteAt.builder()
                .id(1)
                .userId(10)
                .productId(50)
                .name("John Doe")
                .rating(5)
                .comment("Excellent product!")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .deletedAt("2024-06-01T10:00:00Z")
                .build();
    }

    private static ReviewDetailResponse aReviewDetailResponse() {
        return ReviewDetailResponse.builder()
                .id(1)
                .reviewId(10)
                .type("IMAGE")
                .url("https://url.com/image1.jpg")
                .caption("Great product view")
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static ReviewRelationsDetailResponse aReviewRelationsDetailResponse() {
        return ReviewRelationsDetailResponse.builder()
                .id(1)
                .userId(10)
                .productId(50)
                .name("John Doe")
                .comment("Excellent product!")
                .rating(5)
                .reviewDetail(List.of(aReviewDetailResponse()))
                .createdAt("2024-01-01T10:00:00Z")
                .updatedAt("2024-01-01T10:00:00Z")
                .build();
    }

    private static PagedResult<ReviewResponse> aPagedResultReview() {
        return new PagedResult<>(List.of(aReviewResponse()), 1);
    }

    private static PagedResult<ReviewResponseDeleteAt> aPagedResultReviewDeleteAt() {
        return new PagedResult<>(List.of(aReviewResponseDeleteAt()), 1);
    }

    private static PagedResult<ReviewRelationsDetailResponse> aPagedResultReviewDetail() {
        return new PagedResult<>(List.of(aReviewRelationsDetailResponse()), 1);
    }

    @Test
    @DisplayName("findAll delegates and returns paginated response")
    void findAll(VertxTestContext ctx) {
        when(service.getAllReviews(any())).thenReturn(Future.succeededFuture(aPagedResultReview()));

        handler.findAll(FindAllReviewRequest.newBuilder().setPage(1).setPageSize(10).setSearch("Laptop").build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findAll delegates error when service fails")
    void findAllError(VertxTestContext ctx) {
        when(service.getAllReviews(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findAll(FindAllReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByProduct delegates and returns paginated detail response")
    void findByProduct(VertxTestContext ctx) {
        when(service.getReviewByProduct(any())).thenReturn(Future.succeededFuture(aPagedResultReviewDetail()));

        handler.findByProduct(FindAllReviewProductRequest.newBuilder().setProductId(50).setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByProduct delegates error when service fails")
    void findByProductError(VertxTestContext ctx) {
        when(service.getReviewByProduct(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByProduct(FindAllReviewProductRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByMerchant delegates and returns paginated detail response")
    void findByMerchant(VertxTestContext ctx) {
        when(service.getReviewByMerchant(any())).thenReturn(Future.succeededFuture(aPagedResultReviewDetail()));

        handler.findByMerchant(FindAllReviewMerchantRequest.newBuilder().setMerchantId(20).setPage(1).setPageSize(10).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getDataCount()).isEqualTo(1);
                    assertThat(resp.getData(0).getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByMerchant delegates error when service fails")
    void findByMerchantError(VertxTestContext ctx) {
        when(service.getReviewByMerchant(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByMerchant(FindAllReviewMerchantRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByActive delegates and returns paginated response")
    void findByActive(VertxTestContext ctx) {
        when(service.getActiveReviews(any())).thenReturn(Future.succeededFuture(aPagedResultReviewDeleteAt()));

        handler.findByActive(FindAllReviewRequest.newBuilder().setPage(1).setPageSize(10).build())
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
        when(service.getActiveReviews(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByActive(FindAllReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findByTrashed delegates and returns paginated response")
    void findByTrashed(VertxTestContext ctx) {
        when(service.getTrashedReviews(any())).thenReturn(Future.succeededFuture(aPagedResultReviewDeleteAt()));

        handler.findByTrashed(FindAllReviewRequest.newBuilder().setPage(1).setPageSize(10).build())
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
        when(service.getTrashedReviews(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.findByTrashed(FindAllReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates and returns response")
    void findById(VertxTestContext ctx) {
        when(service.getReviewById(anyLong())).thenReturn(Future.succeededFuture(aReviewResponse()));

        handler.findById(pb.review.ReviewCommon.FindByIdReviewRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getComment()).isEqualTo("Excellent product!");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("findById delegates error when service fails")
    void findByIdError(VertxTestContext ctx) {
        when(service.getReviewById(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("Not found")));

        handler.findById(pb.review.ReviewCommon.FindByIdReviewRequest.newBuilder().setId(99).build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}