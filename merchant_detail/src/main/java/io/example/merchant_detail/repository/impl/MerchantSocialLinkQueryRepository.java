package io.example.merchant_detail.repository.impl;

import io.example.merchant_detail.model.MerchantSocialMediaLink;
import io.example.merchant_detail.repository.MerchantSocialLinkeQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantSocialLinkQueryRepository implements MerchantSocialLinkeQueryRepository {
    private final Pool pool;

    @Override
    public Future<MerchantSocialMediaLink> getByMerchantId(Integer merchantId) {
        return pool
                .preparedQuery("""
                        SELECT merchant_social_id, merchant_detail_id, platform, url, created_at, updated_at, deleted_at
                        FROM merchant_social_media_links
                        WHERE merchant_detail_id = $1 AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(merchantId))
                .map(rows -> rows.iterator().hasNext() ? MerchantSocialMediaLink.fromRow(rows.iterator().next())
                        : null);
    }

    @Override
    public Future<MerchantSocialMediaLink> findByTrashedId(Integer socialLinkId) {
        return pool
                .preparedQuery("""
                        SELECT merchant_social_id, merchant_detail_id, platform, url, created_at, updated_at, deleted_at
                        FROM merchant_social_media_links
                        WHERE merchant_social_id = $1 AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of((long) socialLinkId))
                .map(rows -> rows.iterator().hasNext() ? MerchantSocialMediaLink.fromRow(rows.iterator().next())
                        : null);
    }
}