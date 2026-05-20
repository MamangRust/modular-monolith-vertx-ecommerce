package io.example.review_detail.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.review_detail.model.FindAllReview;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.repository.ReviewDetailQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ReviewDetailQueryRepositoryImpl implements ReviewDetailQueryRepository {
    private final Pool client;

    public ReviewDetailQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<ReviewDetail>> getReviewDetails(FindAllReview req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at,
                            COUNT(*) OVER () AS total_count
                        FROM review_details rd
                        WHERE
                            LOWER(COALESCE(caption, '')) LIKE LOWER(CONCAT('%', $1::text, '%'))
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedReviewDetails);
    }

    @Override
    public Future<PagedResult<ReviewDetail>> getReviewDetailsActive(FindAllReview req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM review_details rd
                        WHERE
                            deleted_at IS NULL
                            AND LOWER(COALESCE(caption, '')) LIKE LOWER(CONCAT('%', $1::text, '%'))
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedReviewDetails);
    }

    @Override
    public Future<PagedResult<ReviewDetail>> getReviewDetailsTrashed(FindAllReview req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM review_details rd
                        WHERE
                            deleted_at IS NOT NULL
                            AND LOWER(COALESCE(caption, '')) LIKE LOWER(CONCAT('%', $1::text, '%'))
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedReviewDetails);
    }

    @Override
    public Future<ReviewDetail> getReviewDetail(Integer reviewDetailId) {
        return client
                .preparedQuery("""
                        SELECT
                            review_detail_id,
                            review_id,
                            type,
                            url,
                            caption,
                            created_at,
                            updated_at
                        FROM review_details
                        WHERE
                            review_detail_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(reviewDetailId))
                .map(this::mapSingleOrNull);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return search;
    }

    private ReviewDetail mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? ReviewDetail.fromRow(rows.iterator().next()) : null;
    }

    private PagedResult<ReviewDetail> mapPagedReviewDetails(RowSet<Row> rows) {
        List<ReviewDetail> items = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            items.add(ReviewDetail.fromRow(row));
            if (total == 0) {
                total = row.getInteger("total_count");
            }
        }

        return new PagedResult<>(items, total);
    }
}
