package io.example.review.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.review.model.Review;
import io.example.review.model.ReviewRelationsDetail;
import io.example.review.model.FindAllReview;
import io.example.review.model.FindAllReviewByProduct;
import io.example.review.model.FindAllReviewByMerchant;
import io.example.review.repository.ReviewQueryRepository;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {
    private final Pool client;

    public ReviewQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<Review>> getReviews(FindAllReview req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            review_id, user_id, product_id, name, comment, rating,
                            created_at, updated_at, deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM reviews
                        WHERE deleted_at IS NULL
                          AND ($1::TEXT IS NULL OR review_id::TEXT ILIKE '%' || $1 || '%' OR name ILIKE '%' || $1 || '%')
                        ORDER BY created_at DESC
                        LIMIT $2 OFFSET $3
                        """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedReviews);
    }

    @Override
    public Future<PagedResult<Review>> getReviewsActive(FindAllReview req) {
        return getReviews(req);
    }

    @Override
    public Future<PagedResult<Review>> getReviewsTrashed(FindAllReview req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            review_id, user_id, product_id, name, comment, rating,
                            created_at, updated_at, deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM reviews
                        WHERE deleted_at IS NOT NULL
                          AND ($1::TEXT IS NULL OR review_id::TEXT ILIKE '%' || $1 || '%' OR name ILIKE '%' || $1 || '%')
                        ORDER BY created_at DESC
                        LIMIT $2 OFFSET $3
                        """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedReviews);
    }

    @Override
    public Future<PagedResult<ReviewRelationsDetail>> getReviewByProduct(FindAllReviewByProduct req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
        Integer ratingFilter = req.getRating() != null && req.getRating() > 0 ? req.getRating() : null;

        return client
                .preparedQuery("""
                        SELECT
                            r.review_id, r.user_id, r.product_id, r.name, r.comment, r.rating,
                            r.created_at, r.updated_at, r.deleted_at,
                            COUNT(*) OVER () AS total_count,
                            COALESCE(
                                (SELECT json_agg(jsonb_build_object('detail_id', rd.review_detail_id, 'type', rd.type, 'url', rd.url, 'caption', rd.caption, 'created_at', rd.created_at))
                                 FROM review_details rd WHERE rd.review_id = r.review_id), '[]'
                            ) AS review_details
                        FROM reviews r
                        WHERE r.deleted_at IS NULL
                          AND r.product_id = $1
                          AND ($2::INT IS NULL OR r.rating = $2)
                        ORDER BY r.created_at DESC
                        LIMIT $3 OFFSET $4
                        """)
                .execute(Tuple.of(req.getProductId(), ratingFilter, req.getPageSize(), offset))
                .map(this::mapPagedRelations);
    }

    @Override
    public Future<PagedResult<ReviewRelationsDetail>> getReviewByMerchantId(FindAllReviewByMerchant req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
        Integer ratingFilter = req.getRating() != null && req.getRating() > 0 ? req.getRating() : null;

        return client
                .preparedQuery("""
                        SELECT
                            r.review_id, r.user_id, r.product_id, r.name, r.comment, r.rating,
                            r.created_at, r.updated_at, r.deleted_at,
                            COUNT(*) OVER () AS total_count,
                            COALESCE(
                                (SELECT json_agg(jsonb_build_object('detail_id', rd.review_detail_id, 'type', rd.type, 'url', rd.url, 'caption', rd.caption, 'created_at', rd.created_at))
                                 FROM review_details rd WHERE rd.review_id = r.review_id), '[]'
                            ) AS review_details
                        FROM reviews r
                        JOIN products p ON r.product_id = p.product_id
                        WHERE r.deleted_at IS NULL
                          AND p.merchant_id = $1
                          AND ($2::INT IS NULL OR r.rating = $2)
                        ORDER BY r.created_at DESC
                        LIMIT $3 OFFSET $4
                        """)
                .execute(Tuple.of(req.getMerchantId(), ratingFilter, req.getPageSize(), offset))
                .map(this::mapPagedRelations);
    }

    @Override
    public Future<Review> getReviewById(Long reviewId) {
        return client
                .preparedQuery("""
                        SELECT review_id, user_id, product_id, name, comment, rating, created_at, updated_at, deleted_at
                        FROM reviews
                        WHERE review_id = $1 AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(reviewId))
                .map(rows -> rows.iterator().hasNext() ? Review.fromRow(rows.iterator().next()) : null);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return search;
    }

    private PagedResult<Review> mapPagedReviews(RowSet<Row> rows) {
        List<Review> list = new ArrayList<>();
        int total = 0;
        for (Row row : rows) {
            list.add(Review.fromRow(row));
            if (total == 0) total = row.getInteger("total_count");
        }
        return new PagedResult<>(list, total);
    }

    private PagedResult<ReviewRelationsDetail> mapPagedRelations(RowSet<Row> rows) {
        List<ReviewRelationsDetail> list = new ArrayList<>();
        int total = 0;
        for (Row row : rows) {
            list.add(ReviewRelationsDetail.fromRow(row));
            if (total == 0) total = row.getInteger("total_count");
        }
        return new PagedResult<>(list, total);
    }
}
