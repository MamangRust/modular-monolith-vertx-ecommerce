package io.example.merchant_detail.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.domain.requests.FindAllMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetail;
import io.example.merchant_detail.model.MerchantDetailsRelation;
import io.example.merchant_detail.repository.MerchantDetailQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDetailQueryRepositoryImpl implements MerchantDetailQueryRepository {
    private final Pool pool;

    @Override
    public Future<PagedResult<MerchantDetailsRelation>> getMerchantDetails(FindAllMerchantDetailRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
        String searchPattern = normalizeSearch(req.getSearch());

        return pool
                .preparedQuery(
                        """
                                SELECT
                                    md.merchant_detail_id, md.merchant_id, md.display_name, md.cover_image_url,
                                    md.logo_url, md.short_description, md.website_url, md.created_at, md.updated_at, md.deleted_at,
                                    m.name AS merchant_name,
                                    COUNT(*) OVER () AS total_count,
                                    COALESCE(
                                        json_agg(
                                            json_build_object(
                                                'id', sml.merchant_social_id,
                                                'platform', sml.platform,
                                                'url', sml.url
                                            )
                                        ) FILTER (WHERE sml.merchant_social_id IS NOT NULL), '[]'
                                    ) AS social_media_links
                                FROM merchant_details md
                                JOIN merchants m ON md.merchant_id = m.merchant_id
                                LEFT JOIN merchant_social_media_links sml ON sml.merchant_detail_id = md.merchant_detail_id AND sml.deleted_at IS NULL
                                WHERE md.deleted_at IS NULL
                                  AND m.deleted_at IS NULL
                                  AND ($1::TEXT IS NULL OR m.name ILIKE '%' || $1 || '%')
                                GROUP BY md.merchant_detail_id, m.merchant_id, m.name
                                ORDER BY md.created_at DESC
                                LIMIT $2 OFFSET $3
                                """)
                .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
                .map(this::mapPagedRelation);
    }

    @Override
    public Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsActive(FindAllMerchantDetailRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
        String searchPattern = normalizeSearch(req.getSearch());

        return pool
                .preparedQuery(
                        """
                                SELECT
                                    md.merchant_detail_id, md.merchant_id, md.display_name, md.cover_image_url,
                                    md.logo_url, md.short_description, md.website_url, md.created_at, md.updated_at, md.deleted_at,
                                    m.name AS merchant_name,
                                    COUNT(*) OVER () AS total_count,
                                    COALESCE(
                                        json_agg(
                                            json_build_object(
                                                'id', sml.merchant_social_id,
                                                'platform', sml.platform,
                                                'url', sml.url
                                            )
                                        ) FILTER (WHERE sml.merchant_social_id IS NOT NULL), '[]'
                                    ) AS social_media_links
                                FROM merchant_details md
                                JOIN merchants m ON md.merchant_id = m.merchant_id
                                LEFT JOIN merchant_social_media_links sml ON sml.merchant_detail_id = md.merchant_detail_id AND sml.deleted_at IS NULL
                                WHERE md.deleted_at IS NULL
                                  AND m.deleted_at IS NULL
                                  AND ($1::TEXT IS NULL OR m.name ILIKE '%' || $1 || '%')
                                GROUP BY md.merchant_detail_id, m.merchant_id, m.name
                                ORDER BY md.created_at DESC
                                LIMIT $2 OFFSET $3
                                """)
                .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
                .map(this::mapPagedRelation);
    }

    @Override
    public Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsTrashed(FindAllMerchantDetailRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
        String searchPattern = normalizeSearch(req.getSearch());

        return pool
                .preparedQuery(
                        """
                                SELECT
                                    md.merchant_detail_id, md.merchant_id, md.display_name, md.cover_image_url,
                                    md.logo_url, md.short_description, md.website_url, md.created_at, md.updated_at, md.deleted_at,
                                    m.name AS merchant_name,
                                    COUNT(*) OVER () AS total_count,
                                    COALESCE(
                                        json_agg(
                                            json_build_object(
                                                'id', sml.merchant_social_id,
                                                'platform', sml.platform,
                                                'url', sml.url
                                            )
                                        ) FILTER (WHERE sml.merchant_social_id IS NOT NULL), '[]'
                                    ) AS social_media_links
                                FROM merchant_details md
                                JOIN merchants m ON md.merchant_id = m.merchant_id
                                LEFT JOIN merchant_social_media_links sml ON sml.merchant_detail_id = md.merchant_detail_id AND sml.deleted_at IS NULL
                                WHERE md.deleted_at IS NULL
                                  AND m.deleted_at IS NOT NULL
                                  AND ($1::TEXT IS NULL OR m.name ILIKE '%' || $1 || '%')
                                GROUP BY md.merchant_detail_id, m.merchant_id, m.name
                                ORDER BY md.created_at DESC
                                LIMIT $2 OFFSET $3
                                """)
                .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
                .map(this::mapPagedRelation);
    }

    @Override
    public Future<MerchantDetailsRelation> getMerchantDetail(Long merchantDetailId) {
        return pool
                .preparedQuery(
                        """
                                SELECT
                                    md.merchant_detail_id, md.merchant_id, md.display_name, md.cover_image_url,
                                    md.logo_url, md.short_description, md.website_url, md.created_at, md.updated_at, md.deleted_at,
                                    m.name AS merchant_name,
                                    COALESCE(
                                        json_agg(
                                            json_build_object(
                                                'id', sml.merchant_social_id,
                                                'platform', sml.platform,
                                                'url', sml.url
                                            )
                                        ) FILTER (WHERE sml.merchant_social_id IS NOT NULL), '[]'
                                    ) AS social_media_links
                                FROM merchant_details md
                                JOIN merchants m ON md.merchant_id = m.merchant_id
                                LEFT JOIN merchant_social_media_links sml ON sml.merchant_detail_id = md.merchant_detail_id
                                WHERE md.merchant_detail_id = $1 AND md.deleted_at IS NULL
                                GROUP BY md.merchant_detail_id, m.merchant_id, m.name
                                """)
                .execute(Tuple.of(merchantDetailId))
                .map(rows -> rows.iterator().hasNext() ? MerchantDetailsRelation.fromRow(rows.iterator().next())
                        : null);
    }

    @Override
    public Future<MerchantDetail> findByTrashedId(Long merchantDetailId) {
        return pool
                .preparedQuery("""
                        SELECT
                            merchant_detail_id, merchant_id, display_name, cover_image_url,
                            logo_url, short_description, website_url, created_at, updated_at, deleted_at
                        FROM merchant_details
                        WHERE merchant_detail_id = $1 AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(merchantDetailId))
                .map(rows -> rows.iterator().hasNext() ? MerchantDetail.fromRow(rows.iterator().next()) : null);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search;
    }

    private PagedResult<MerchantDetailsRelation> mapPagedRelation(RowSet<Row> rows) {
        List<MerchantDetailsRelation> list = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            list.add(MerchantDetailsRelation.fromRow(row));
            if (total == 0) {
                total = row.getInteger("total_count");
            }
        }
        return new PagedResult<>(list, total);
    }
}
