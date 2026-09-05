package io.example.merchant.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant.domain.requests.FindAllMerchantRequest;
import io.example.merchant.model.Merchant;
import io.example.merchant.repository.MerchantQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantQueryRepositoryImpl implements MerchantQueryRepository {
  private final Pool client;

  @Override
  public Future<PagedResult<Merchant>> getMerchants(FindAllMerchantRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    return client
        .preparedQuery(
            """
                SELECT merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
                FROM merchants
                WHERE ($1::TEXT IS NULL OR name ILIKE '%' || $1 || '%')
                ORDER BY created_at ASC LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedMerchants);
  }

  @Override
  public Future<PagedResult<Merchant>> getActiveMerchants(FindAllMerchantRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    return client
        .preparedQuery(
            """
                SELECT merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
                FROM merchants
                WHERE deleted_at IS NULL AND ($1::TEXT IS NULL OR name ILIKE '%' || $1 || '%')
                ORDER BY created_at ASC LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedMerchants);
  }

  @Override
  public Future<PagedResult<Merchant>> getTrashedMerchants(FindAllMerchantRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    return client
        .preparedQuery(
            """
                SELECT merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
                FROM merchants
                WHERE deleted_at IS NOT NULL AND ($1::TEXT IS NULL OR name ILIKE '%' || $1 || '%')
                ORDER BY deleted_at DESC LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedMerchants);
  }

  @Override
  public Future<Merchant> getMerchantById(Long merchantId) {
    return client
        .preparedQuery("""
            SELECT merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
            FROM merchants
            WHERE merchant_id = $1 AND deleted_at IS NULL
            """)
        .execute(Tuple.of(merchantId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Merchant> findByTrashedId(Long merchantId) {
    return client
        .preparedQuery("""
            SELECT merchant_id, merchant_no, name, description, address, contact_email, contact_phone, user_id, status, created_at, updated_at, deleted_at
            FROM merchants
            WHERE merchant_id = $1 AND deleted_at IS NOT NULL
            """)
        .execute(Tuple.of(merchantId))
        .map(this::mapSingleOrNull);
  }

  private String normalizeSearch(String search) {
    return (search == null || search.isBlank()) ? null : search;
  }

  private Merchant mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? Merchant.fromRow(rows.iterator().next()) : null;
  }

  private PagedResult<Merchant> mapPagedMerchants(RowSet<Row> rows) {
    List<Merchant> merchants = new ArrayList<>();
    int total = 0;
    for (Row row : rows) {
      merchants.add(Merchant.fromRow(row));
      if (total == 0) {
        Integer tc = row.getInteger("total_count");
        if (tc != null)
          total = tc;
      }
    }
    return new PagedResult<>(merchants, total);
  }
}
