package io.example.merchant_policy.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.model.MerchantPolicyRelation;
import io.example.merchant_policy.repository.MerchantPoliciesQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantPoliciesQueryRepositoryImpl implements MerchantPoliciesQueryRepository {
  private final Pool client;

  public MerchantPoliciesQueryRepositoryImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<PagedResult<MerchantPolicyRelation>> getMerchantPolicies(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;

    return client
        .preparedQuery("""
            SELECT
                mp.merchant_policy_id, mp.merchant_id, mp.policy_type, mp.title, mp.description,
                mp.created_at, mp.updated_at, mp.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_policies mp
            JOIN merchants m ON mp.merchant_id = m.merchant_id
            WHERE mp.deleted_at IS NULL
              AND m.deleted_at IS NULL
              AND m.name ILIKE '%' || $1 || '%'
            ORDER BY mp.created_at DESC
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedRelations);
  }

  @Override
  public Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesActive(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;

    return client
        .preparedQuery("""
            SELECT
                mp.merchant_policy_id, mp.merchant_id, mp.policy_type, mp.title, mp.description,
                mp.created_at, mp.updated_at, mp.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_policies mp
            JOIN merchants m ON mp.merchant_id = m.merchant_id
            WHERE m.deleted_at IS NULL
              AND mp.deleted_at IS NULL
              AND LOWER(m.name) LIKE LOWER(CONCAT('%', $1::text, '%'))
            ORDER BY mp.created_at DESC
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedRelations);
  }

  @Override
  public Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesTrashed(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;

    return client
        .preparedQuery("""
            SELECT
                mp.merchant_policy_id, mp.merchant_id, mp.policy_type, mp.title, mp.description,
                mp.created_at, mp.updated_at, mp.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_policies mp
            JOIN merchants m ON mp.merchant_id = m.merchant_id
            WHERE mp.deleted_at IS NOT NULL
              AND LOWER(m.name) LIKE LOWER(CONCAT('%', $1::text, '%'))
            ORDER BY mp.created_at DESC
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedRelations);
  }

  @Override
  public Future<MerchantPolicy> getMerchantPolicy(Long id) {
    return client
        .preparedQuery("""
            SELECT merchant_policy_id, merchant_id, policy_type, title, description, created_at, updated_at, deleted_at
            FROM merchant_policies
            WHERE merchant_policy_id = $1 AND deleted_at IS NULL;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantPolicy.fromRow(rows.iterator().next()) : null);
  }

  private String normalizeSearch(String search) {
    if (search == null || search.isBlank()) {
      return "";
    }
    return search;
  }

  private PagedResult<MerchantPolicyRelation> mapPagedRelations(RowSet<Row> rows) {
    List<MerchantPolicyRelation> list = new ArrayList<>();
    int total = 0;
    for (Row row : rows) {
      list.add(MerchantPolicyRelation.fromRow(row));
      if (total == 0) {
        total = row.getInteger("total_count");
      }
    }
    return new PagedResult<>(list, total);
  }
}
