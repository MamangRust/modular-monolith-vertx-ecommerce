package io.example.review_detail.repository.impl;

import io.example.review_detail.model.CreateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.model.UpdateReviewDetailRequest;
import io.example.review_detail.repository.ReviewDetailCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ReviewDetailCommandRepositoryImpl implements ReviewDetailCommandRepository {
    private final Pool client;

    public ReviewDetailCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<ReviewDetail> createReviewDetail(CreateReviewDetailRequest req) {
        return client
                .preparedQuery("""
                        INSERT INTO
                            review_details (review_id, type, url, caption)
                        VALUES ($1, $2, $3, $4)
                        RETURNING
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at
                        """)
                .execute(Tuple.of(req.getReviewId(), req.getType(), req.getFile(), req.getCaption()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ReviewDetail> updateReviewDetail(UpdateReviewDetailRequest req) {
        return client
                .preparedQuery("""
                        UPDATE review_details
                        SET
                            type = $1,
                            url = $2,
                            caption = $3
                        WHERE
                            review_detail_id = $4
                        RETURNING
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at
                        """)
                .execute(Tuple.of(req.getType(), req.getFile(), req.getCaption(), req.getReviewDetailId()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ReviewDetail> trashReviewDetail(Integer reviewDetailId) {
        return client
                .preparedQuery("""
                        UPDATE review_details
                        SET
                            deleted_at = CURRENT_TIMESTAMP
                        WHERE
                            review_detail_id = $1
                            AND deleted_at IS NULL
                        RETURNING
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(reviewDetailId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ReviewDetail> restoreReviewDetail(Integer reviewDetailId) {
        return client
                .preparedQuery("""
                        UPDATE review_details
                        SET
                            deleted_at = NULL
                        WHERE
                            review_detail_id = $1
                            AND deleted_at IS NOT NULL
                        RETURNING
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(reviewDetailId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Void> deletePermanentReviewDetail(Integer reviewDetailId) {
        return client
                .preparedQuery("""
                        DELETE FROM review_details
                        WHERE
                            review_detail_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(reviewDetailId))
                .mapEmpty();
    }

    @Override
    public Future<Void> restoreAllReviewDetails() {
        return client
                .preparedQuery("""
                        UPDATE review_details
                        SET
                            deleted_at = NULL
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteAllPermanentReviewDetails() {
        return client
                .preparedQuery("DELETE FROM review_details WHERE deleted_at IS NOT NULL")
                .execute()
                .mapEmpty();
    }

    private ReviewDetail mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? ReviewDetail.fromRow(rows.iterator().next()) : null;
    }
}
