package io.example.review.handler;

import com.google.protobuf.Empty;

import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.service.ReviewCommandService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pb.review.ReviewCommon.FindByIdReviewRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class ReviewCommandHandlerTest {

    @Mock
    private ReviewCommandService service;

    private ReviewCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReviewCommandHandler(service);
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

    @Test
    @DisplayName("create delegates and returns response")
    void create(VertxTestContext ctx) {
        when(service.createReview(any())).thenReturn(Future.succeededFuture(aReviewResponse()));

        var req = pb.review.ReviewCommand.CreateReviewRequest.newBuilder()
                .setUserId(10)
                .setProductId(50)
                .setName("John Doe")
                .setRating(5)
                .setComment("Excellent product!")
                .build();

        handler.create(req)
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("OK");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().getRating()).isEqualTo(5);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("create delegates error when service fails")
    void createError(VertxTestContext ctx) {
        when(service.createReview(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.create(pb.review.ReviewCommand.CreateReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("update delegates and returns response")
    void update(VertxTestContext ctx) {
        when(service.updateReview(any())).thenReturn(Future.succeededFuture(aReviewResponse()));

        var req = pb.review.ReviewCommand.UpdateReviewRequest.newBuilder()
                .setReviewId(1)
                .setName("John Doe Updated")
                .setRating(4)
                .setComment("Good product")
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
        when(service.updateReview(any())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.update(pb.review.ReviewCommand.UpdateReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedReview delegates and returns response")
    void trashedReview(VertxTestContext ctx) {
        when(service.trashReview(anyLong())).thenReturn(Future.succeededFuture(aReviewResponseDeleteAt()));

        handler.trashedReview(FindByIdReviewRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    assertThat(resp.getData().hasDeletedAt()).isTrue();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("trashedReview delegates error when service fails")
    void trashedReviewError(VertxTestContext ctx) {
        when(service.trashReview(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.trashedReview(FindByIdReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreReview delegates and returns response")
    void restoreReview(VertxTestContext ctx) {
        when(service.restoreReview(anyLong())).thenReturn(Future.succeededFuture(aReviewResponseDeleteAt()));

        handler.restoreReview(FindByIdReviewRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getData().getId()).isEqualTo(1);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreReview delegates error when service fails")
    void restoreReviewError(VertxTestContext ctx) {
        when(service.restoreReview(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreReview(FindByIdReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteReviewPermanent delegates and returns success")
    void deleteReviewPermanent(VertxTestContext ctx) {
        when(service.deleteReviewPermanently(anyLong())).thenReturn(Future.succeededFuture());

        handler.deleteReviewPermanent(FindByIdReviewRequest.newBuilder().setId(1).build())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("Review deleted permanently");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteReviewPermanent delegates error when service fails")
    void deleteReviewPermanentError(VertxTestContext ctx) {
        when(service.deleteReviewPermanently(anyLong())).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteReviewPermanent(FindByIdReviewRequest.newBuilder().build())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllReview delegates and returns success")
    void restoreAllReview(VertxTestContext ctx) {
        when(service.restoreAllReviews()).thenReturn(Future.succeededFuture());

        handler.restoreAllReview(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All reviews restored successfully");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("restoreAllReview delegates error when service fails")
    void restoreAllReviewError(VertxTestContext ctx) {
        when(service.restoreAllReviews()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.restoreAllReview(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllReviewPermanent delegates and returns success")
    void deleteAllReviewPermanent(VertxTestContext ctx) {
        when(service.deleteAllPermanentReviews()).thenReturn(Future.succeededFuture());

        handler.deleteAllReviewPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
                    assertThat(resp.getStatus()).isEqualTo("success");
                    assertThat(resp.getMessage()).isEqualTo("All reviews permanently deleted");
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("deleteAllReviewPermanent delegates error when service fails")
    void deleteAllReviewPermanentError(VertxTestContext ctx) {
        when(service.deleteAllPermanentReviews()).thenReturn(Future.failedFuture(new RuntimeException("DB error")));

        handler.deleteAllReviewPermanent(Empty.getDefaultInstance())
                .onComplete(ctx.failing(err -> ctx.verify(() -> {
                    assertThat(err).isInstanceOf(io.grpc.StatusRuntimeException.class);
                    ctx.completeNow();
                })));
    }
}