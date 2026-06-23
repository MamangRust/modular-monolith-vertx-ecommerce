package io.example.merchant.repository.impl;

import io.example.merchant.domain.requests.CreateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantStatusRequest;
import io.example.merchant.model.Merchant;
import io.example.merchant.repository.MerchantCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantCommandRepositoryImpl implements MerchantCommandRepository {
  private final Pool client;

  @Override
  public Future<Merchant> createMerchant(CreateMerchantRequest request) {
    return client
        .preparedQuery("""
            INSERT INTO merchants (user_id, name, api_key, status)
            VALUES ($1, $2, $3, $4)
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(request.getUserId(), request.getName(), request.getApiKey(), request.getStatus()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateMerchant(UpdateMerchantRequest request) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET name = $1, status = $2, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $3 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(request.getName(), request.getStatus(), request.getMerchantId()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateStatus(UpdateMerchantStatusRequest request) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET status = $1, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $2 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, api_key, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(request.getStatus(), request.getMerchantId()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> trashMerchant(Long merchantId) {
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
  public Future<Merchant> restoreMerchant(Long merchantId) {
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
  public Future<Boolean> deleteMerchantPermanently(Long merchantId) {
    return client
        .preparedQuery("DELETE FROM merchants WHERE merchant_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(merchantId))
        .map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Future<Integer> restoreAllMerchants() {
    return client
        .query("UPDATE merchants SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAllPermanentMerchants() {
    return client
        .query("DELETE FROM merchants WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  private Merchant mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? Merchant.fromRow(rows.iterator().next()) : null;
  }
}
