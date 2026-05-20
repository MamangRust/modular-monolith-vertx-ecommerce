package io.example.merchant.repository.impl;

import io.example.merchant.model.Merchant;
import io.example.merchant.repository.MerchantCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantCommandRepositoryImpl implements MerchantCommandRepository {
  private final Pool client;

  public MerchantCommandRepositoryImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<Merchant> createMerchant(Integer userId, String name, String apiKey, String status) {
    return client
        .preparedQuery("""
            INSERT INTO merchants (user_id, name, api_key, status)
            VALUES ($1, $2, $3, $4)
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(userId, name, apiKey, status))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateMerchant(Integer merchantId, String name, String status) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET name = $1, status = $2, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $3 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(name, status, merchantId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateStatus(Integer merchantId, String status) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET status = $1, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $2 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(status, merchantId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> trashMerchant(Integer merchantId) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $1 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(merchantId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> restoreMerchant(Integer merchantId) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET deleted_at = NULL
            WHERE merchant_id = $1 AND deleted_at IS NOT NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(merchantId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Void> deleteMerchantPermanently(Integer merchantId) {
    return client
        .preparedQuery("DELETE FROM merchants WHERE merchant_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(merchantId))
        .mapEmpty();
  }

  @Override
  public Future<Void> restoreAllMerchants() {
    return client
        .query("UPDATE merchants SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .mapEmpty();
  }

  @Override
  public Future<Void> deleteAllPermanentMerchants() {
    return client
        .query("DELETE FROM merchants WHERE deleted_at IS NOT NULL")
        .execute()
        .mapEmpty();
  }

  private Merchant mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? Merchant.fromRow(rows.iterator().next()) : null;
  }
}
