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
            INSERT INTO merchants (user_id, name, description, address, contact_email, contact_phone, status)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
            RETURNING merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(request.getUserId(), request.getName(),
            request.getDescription() != null ? request.getDescription() : "",
            request.getAddress() != null ? request.getAddress() : "",
            request.getContactEmail() != null ? request.getContactEmail() : "",
            request.getContactPhone() != null ? request.getContactPhone() : "",
            request.getStatus()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateMerchant(UpdateMerchantRequest request) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET name = COALESCE(NULLIF($1, ''), name),
                description = COALESCE(NULLIF($2, ''), description),
                address = COALESCE(NULLIF($3, ''), address),
                contact_email = COALESCE(NULLIF($4, ''), contact_email),
                contact_phone = COALESCE(NULLIF($5, ''), contact_phone),
                status = COALESCE(NULLIF($6, ''), status),
                updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $7 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(
            request.getName() != null ? request.getName() : "",
            request.getDescription() != null ? request.getDescription() : "",
            request.getAddress() != null ? request.getAddress() : "",
            request.getContactEmail() != null ? request.getContactEmail() : "",
            request.getContactPhone() != null ? request.getContactPhone() : "",
            request.getStatus() != null ? request.getStatus() : "",
            request.getMerchantId()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> updateStatus(UpdateMerchantStatusRequest request) {
    return client
        .preparedQuery("""
            UPDATE merchants
            SET status = $1, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_id = $2 AND deleted_at IS NULL
            RETURNING merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
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
            RETURNING merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
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
            RETURNING merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
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
