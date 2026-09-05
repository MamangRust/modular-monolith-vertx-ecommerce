package io.example.merchant_policy.repository.impl;

import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.repository.MerchantPoliciesCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import io.example.merchant_policy.domain.requests.CreateMerchantPoliciesRequest;
import io.example.merchant_policy.domain.requests.UpdateMerchantPoliciesRequest;

@RequiredArgsConstructor
public class MerchantPoliciesCommandRepositoryImpl implements MerchantPoliciesCommandRepository {
  private final Pool client;

  @Override
  public Future<MerchantPolicy> create(CreateMerchantPoliciesRequest req) {
    return client
        .preparedQuery("""
            INSERT INTO merchant_policies (merchant_id, policy_type, title, description)
            VALUES ($1, $2, $3, $4)
            RETURNING *;
            """)
        .execute(Tuple.of(req.getMerchantId(), req.getPolicyType(), req.getTitle(), req.getDescription()))
        .map(rows -> MerchantPolicy.fromRow(rows.iterator().next()));
  }

  @Override
  public Future<MerchantPolicy> update(UpdateMerchantPoliciesRequest req) {
    return client
        .preparedQuery("""
            UPDATE merchant_policies
            SET policy_type = COALESCE(NULLIF($2, ''), policy_type), title = COALESCE(NULLIF($3, ''), title), description = COALESCE(NULLIF($4, ''), description), updated_at = CURRENT_TIMESTAMP
            WHERE merchant_policy_id = $1 AND deleted_at IS NULL
            RETURNING *;
            """)
        .execute(Tuple.of((long) req.getMerchantPolicyId(), req.getPolicyType() != null ? req.getPolicyType() : "", req.getTitle() != null ? req.getTitle() : "", req.getDescription() != null ? req.getDescription() : ""))
        .map(rows -> rows.iterator().hasNext() ? MerchantPolicy.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantPolicy> trash(Long id) {
    return client
        .preparedQuery(
            "UPDATE merchant_policies SET deleted_at = CURRENT_TIMESTAMP WHERE merchant_policy_id = $1 AND deleted_at IS NULL RETURNING *")
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantPolicy.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantPolicy> restore(Long id) {
    return client
        .preparedQuery(
            "UPDATE merchant_policies SET deleted_at = NULL WHERE merchant_policy_id = $1 AND deleted_at IS NOT NULL RETURNING *")
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantPolicy.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<Boolean> deletePermanent(Long id) {
    return client
        .preparedQuery("DELETE FROM merchant_policies WHERE merchant_policy_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(id))
        .map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Future<Integer> restoreAll() {
    return client
        .preparedQuery("UPDATE merchant_policies SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAllPermanent() {
    return client
        .preparedQuery("DELETE FROM merchant_policies WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }
}
