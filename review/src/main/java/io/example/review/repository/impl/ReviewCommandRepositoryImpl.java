package io.example.review.repository.impl;

import io.example.review.model.Review;
import io.example.review.domain.requests.CreateReviewRequest;
import io.example.review.domain.requests.UpdateReviewRequest;
import io.example.review.repository.ReviewCommandRepository;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewCommandRepositoryImpl implements ReviewCommandRepository {
    private final Pool client;

    @Override
    public Future<Review> createReview(CreateReviewRequest req) {
        return client
                .preparedQuery("""
                        INSERT INTO reviews (user_id, product_id, name, comment, rating)
                        VALUES ($1, $2, $3, $4, $5)
                        RETURNING *
                        """)
                .execute(
                        Tuple.of(req.getUserId(), req.getProductId(), req.getName(), req.getComment(), req.getRating()))
                .map(rows -> Review.fromRow(rows.iterator().next()));
    }

    @Override
    public Future<Review> updateReview(UpdateReviewRequest req) {
        return client
                .preparedQuery("""
                        UPDATE reviews
                        SET name = COALESCE(NULLIF($2, ''), name), comment = COALESCE(NULLIF($3, ''), comment), rating = COALESCE(NULLIF($4::INT, 0), rating), updated_at = CURRENT_TIMESTAMP
                        WHERE review_id = $1 AND deleted_at IS NULL
                        RETURNING *
                        """)
                .execute(Tuple.of(req.getReviewId(), req.getName() != null ? req.getName() : "", req.getComment() != null ? req.getComment() : "", req.getRating()))
                .map(rows -> rows.iterator().hasNext() ? Review.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Review> trashReview(Long reviewId) {
        return client
                .preparedQuery(
                        "UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE review_id = $1 AND deleted_at IS NULL RETURNING *")
                .execute(Tuple.of(reviewId))
                .map(rows -> rows.iterator().hasNext() ? Review.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Review> restoreReview(Long reviewId) {
        return client
                .preparedQuery(
                        "UPDATE reviews SET deleted_at = NULL WHERE review_id = $1 AND deleted_at IS NOT NULL RETURNING *")
                .execute(Tuple.of(reviewId))
                .map(rows -> rows.iterator().hasNext() ? Review.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Void> deleteReviewPermanently(Long reviewId) {
        return client
                .preparedQuery("DELETE FROM reviews WHERE review_id = $1 AND deleted_at IS NOT NULL")
                .execute(Tuple.of(reviewId))
                .mapEmpty();
    }

    @Override
    public Future<Integer> restoreAllReviews() {
        return client
                .preparedQuery("UPDATE reviews SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> deleteAllPermanentReviews() {
        return client
                .preparedQuery("DELETE FROM reviews WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }
}
