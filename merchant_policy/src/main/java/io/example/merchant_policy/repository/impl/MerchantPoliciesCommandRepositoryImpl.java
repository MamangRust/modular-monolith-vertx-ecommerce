package io.example.merchant_policy.repository.impl;

import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.repository.MerchantPoliciesCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantPoliciesCommandRepositoryImpl implements MerchantPoliciesCommandRepository {
  private final Pool client;

  public MerchantPoliciesCommandRepositoryImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<MerchantPolicy> create(pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest req) {
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
  public Future<MerchantPolicy> update(pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest req) {
    return client
        .preparedQuery("""
            UPDATE merchant_policies
            SET policy_type = $2, title = $3, description = $4, updated_at = CURRENT_TIMESTAMP
            WHERE merchant_policy_id = $1 AND deleted_at IS NULL
            RETURNING *;
            """)
        .execute(Tuple.of((long) req.getMerchantPolicyId(), req.getPolicyType(), req.getTitle(), req.getDescription()))
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
  public Future<Void> deletePermanent(Long id) {
    return client
        .preparedQuery("DELETE FROM merchant_policies WHERE merchant_policy_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(id))
        .mapEmpty();
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
