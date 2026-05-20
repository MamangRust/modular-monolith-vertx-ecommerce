package io.example.merchant_detail.repository.impl;

import io.example.merchant_detail.model.MerchantSocialMediaLink;
import io.example.merchant_detail.repository.MerchantSocialLinkCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;

public class MerchantSocialLinkCommandRepositoryImpl implements MerchantSocialLinkCommandRepository {
  private final Pool pool;

  public MerchantSocialLinkCommandRepositoryImpl(Pool pool) {
    this.pool = pool;
  }

  @Override
  public Future<MerchantSocialMediaLink> create(CreateMerchantSocialRequest req) {
    return pool
        .preparedQuery("""
            INSERT INTO merchant_social_media_links (merchant_detail_id, platform, url)
            VALUES ($1, $2, $3)
            RETURNING merchant_social_id, merchant_detail_id, platform, url, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(
            req.getMerchantDetailId(),
            req.getPlatform(),
            req.getUrl()))
        .map(rows -> MerchantSocialMediaLink.fromRow(rows.iterator().next()));
  }

  @Override
  public Future<MerchantSocialMediaLink> update(UpdateMerchantSocialRequest req) {
    return pool
        .preparedQuery("""
            UPDATE merchant_social_media_links
            SET platform = $2, url = $3, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_social_id = $1 AND deleted_at IS NULL
            RETURNING merchant_social_id, merchant_detail_id, platform, url, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(
            (long) req.getId(),
            req.getPlatform(),
            req.getUrl()))
        .map(rows -> rows.iterator().hasNext() ? MerchantSocialMediaLink.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantSocialMediaLink> trash(Long id) {
    return pool
        .preparedQuery("UPDATE merchant_social_media_links SET deleted_at = CURRENT_TIMESTAMP WHERE merchant_social_id = $1 AND deleted_at IS NULL RETURNING *")
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantSocialMediaLink.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantSocialMediaLink> restore(Long id) {
    return pool
        .preparedQuery("UPDATE merchant_social_media_links SET deleted_at = NULL WHERE merchant_social_id = $1 AND deleted_at IS NOT NULL RETURNING *")
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantSocialMediaLink.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<Void> deletePermanent(Long id) {
    return pool
        .preparedQuery("DELETE FROM merchant_social_media_links WHERE merchant_social_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(id))
        .mapEmpty();
  }

  @Override
  public Future<Integer> restoreAll() {
    return pool
        .preparedQuery("UPDATE merchant_social_media_links SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAll() {
    return pool
        .preparedQuery("DELETE FROM merchant_social_media_links WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }
}
