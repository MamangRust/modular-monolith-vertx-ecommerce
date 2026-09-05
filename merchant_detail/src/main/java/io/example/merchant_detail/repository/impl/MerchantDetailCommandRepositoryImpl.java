package io.example.merchant_detail.repository.impl;

import io.example.merchant_detail.model.MerchantDetail;
import io.example.merchant_detail.repository.MerchantDetailCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import io.example.merchant_detail.domain.requests.CreateMerchantDetailRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantDetailRequest;

@RequiredArgsConstructor
public class MerchantDetailCommandRepositoryImpl implements MerchantDetailCommandRepository {
        private final Pool pool;

        @Override
        public Future<MerchantDetail> create(CreateMerchantDetailRequest req) {
                return pool
                                .preparedQuery(
                                                """
                                                                INSERT INTO merchant_details (merchant_id, display_name, cover_image_url, logo_url, short_description, website_url)
                                                                VALUES ($1, $2, $3, $4, $5, $6)
                                                                RETURNING *
                                                                """)
                                .execute(Tuple.of(
                                                req.getMerchantId(),
                                                req.getDisplayName(),
                                                req.getCoverImageUrl(),
                                                req.getLogoUrl(),
                                                req.getShortDescription(),
                                                req.getWebsiteUrl()))
                                .map(rows -> MerchantDetail.fromRow(rows.iterator().next()));
        }

        @Override
        public Future<MerchantDetail> update(UpdateMerchantDetailRequest req) {
                return pool
                                .preparedQuery("""
                                                UPDATE merchant_details
                                                SET display_name = COALESCE(NULLIF($2, ''), display_name), cover_image_url = COALESCE(NULLIF($3, ''), cover_image_url), logo_url = COALESCE(NULLIF($4, ''), logo_url),
                                                    short_description = COALESCE(NULLIF($5, ''), short_description), website_url = COALESCE(NULLIF($6, ''), website_url), updated_at = CURRENT_TIMESTAMP
                                                WHERE merchant_detail_id = $1 AND deleted_at IS NULL
                                                RETURNING *
                                                """)
                                .execute(Tuple.of(
                                                (long) req.getMerchantDetailId(),
                                                req.getDisplayName() != null ? req.getDisplayName() : "",
                                                req.getCoverImageUrl() != null ? req.getCoverImageUrl() : "",
                                                req.getLogoUrl() != null ? req.getLogoUrl() : "",
                                                req.getShortDescription() != null ? req.getShortDescription() : "",
                                                req.getWebsiteUrl() != null ? req.getWebsiteUrl() : ""))
                                .map(rows -> rows.iterator().hasNext() ? MerchantDetail.fromRow(rows.iterator().next())
                                                : null);
        }

        @Override
        public Future<MerchantDetail> trash(Long id) {
                return pool
                                .preparedQuery(
                                                "UPDATE merchant_details SET deleted_at = CURRENT_TIMESTAMP WHERE merchant_detail_id = $1 AND deleted_at IS NULL RETURNING *")
                                .execute(Tuple.of(id))
                                .map(rows -> rows.iterator().hasNext() ? MerchantDetail.fromRow(rows.iterator().next())
                                                : null);
        }

        @Override
        public Future<MerchantDetail> restore(Long id) {
                return pool
                                .preparedQuery(
                                                "UPDATE merchant_details SET deleted_at = NULL WHERE merchant_detail_id = $1 AND deleted_at IS NOT NULL RETURNING *")
                                .execute(Tuple.of(id))
                                .map(rows -> rows.iterator().hasNext() ? MerchantDetail.fromRow(rows.iterator().next())
                                                : null);
        }

        @Override
        public Future<Boolean> deletePermanent(Long id) {
                return pool
                                .preparedQuery("""
                                        WITH deleted_links AS (
                                            DELETE FROM merchant_social_media_links WHERE merchant_detail_id = $1
                                        )
                                        DELETE FROM merchant_details WHERE merchant_detail_id = $1 AND deleted_at IS NOT NULL
                                        """)
                                .execute(Tuple.of(id))
                                .map(row -> row.rowCount() > 0);
        }

        @Override
        public Future<Integer> restoreAll() {
                return pool
                                .preparedQuery("UPDATE merchant_details SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                                .execute()
                                .map(RowSet::rowCount);
        }

        @Override
        public Future<Integer> deleteAll() {
                return pool
                                .preparedQuery("DELETE FROM merchant_details WHERE deleted_at IS NOT NULL")
                                .execute()
                                .map(RowSet::rowCount);
        }
}
