package io.example.merchant_award.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantAwardQueryRepositoryImpl implements MerchantAwardQueryRepository {
  private final Pool pool;

  public MerchantAwardQueryRepositoryImpl(Pool pool) {
    this.pool = pool;
  }

  @Override
  public Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwards(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    String searchVal = search != null ? search : "";

    return pool.preparedQuery("""
        SELECT
            mca.merchant_certification_id, mca.merchant_id, mca.title, mca.description,
            mca.issued_by, mca.issue_date, mca.expiry_date, mca.certificate_url,
            mca.created_at, mca.updated_at, mca.deleted_at,
            m.name AS merchant_name,
            COUNT(*) OVER () AS total_count
        FROM merchant_certifications_and_awards mca
        JOIN merchants m ON mca.merchant_id = m.merchant_id
        WHERE mca.deleted_at IS NULL
          AND m.deleted_at IS NULL
          AND m.name ILIKE '%' || $1 || '%'
        ORDER BY mca.created_at DESC
        LIMIT $2 OFFSET $3;
        """)
        .execute(Tuple.of(searchVal, pageSize, offset))
        .map(this::mapPagedResult);
  }

  @Override
  public Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwardsActive(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    String searchVal = search != null ? search : "";

    return pool.preparedQuery("""
        SELECT
            mca.merchant_certification_id, mca.merchant_id, mca.title, mca.description,
            mca.issued_by, mca.issue_date, mca.expiry_date, mca.certificate_url,
            mca.created_at, mca.updated_at, mca.deleted_at,
            m.name AS merchant_name,
            COUNT(*) OVER () AS total_count
        FROM merchant_certifications_and_awards mca
        JOIN merchants m ON mca.merchant_id = m.merchant_id
        WHERE mca.deleted_at IS NULL
          AND m.deleted_at IS NULL
          AND m.name ILIKE '%' || $1 || '%'
        ORDER BY mca.created_at DESC
        LIMIT $2 OFFSET $3;
        """)
        .execute(Tuple.of(searchVal, pageSize, offset))
        .map(this::mapPagedResult);
  }

  @Override
  public Future<PagedResult<MerchantAward>> getTrashedCertificationsAndAwards(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    String searchVal = search != null ? search : "";

    return pool.preparedQuery("""
        SELECT
            mca.merchant_certification_id, mca.merchant_id, mca.title, mca.description,
            mca.issued_by, mca.issue_date, mca.expiry_date, mca.certificate_url,
            mca.created_at, mca.updated_at, mca.deleted_at,
            m.name AS merchant_name,
            COUNT(*) OVER () AS total_count
        FROM merchant_certifications_and_awards mca
        JOIN merchants m ON mca.merchant_id = m.merchant_id
        WHERE mca.deleted_at IS NOT NULL
          AND m.deleted_at IS NULL
          AND m.name ILIKE '%' || $1 || '%'
        ORDER BY mca.created_at DESC
        LIMIT $2 OFFSET $3;
        """)
        .execute(Tuple.of(searchVal, pageSize, offset))
        .map(this::mapPagedResult);
  }

  @Override
  public Future<MerchantAward> getMerchantCertificationOrAward(Long id) {
    return pool.preparedQuery("""
        SELECT
            mca.merchant_certification_id, mca.merchant_id, mca.title, mca.description,
            mca.issued_by, mca.issue_date, mca.expiry_date, mca.certificate_url,
            mca.created_at, mca.updated_at, mca.deleted_at,
            m.name AS merchant_name
        FROM merchant_certifications_and_awards mca
        JOIN merchants m ON mca.merchant_id = m.merchant_id
        WHERE mca.merchant_certification_id = $1
          AND mca.deleted_at IS NULL;
        """)
        .execute(Tuple.of(id))
        .map(rows -> {
          if (rows.iterator().hasNext()) {
            return MerchantAward.fromRow(rows.iterator().next());
          }
          return null;
        });
  }

  private PagedResult<MerchantAward> mapPagedResult(RowSet<Row> rows) {
    List<MerchantAward> list = new ArrayList<>();
    int total = 0;
    for (Row row : rows) {
      list.add(MerchantAward.fromRow(row));
      if (total == 0) {
        Integer tc = row.getInteger("total_count");
        total = tc != null ? tc : 0;
      }
    }
    return new PagedResult<>(list, total);
  }
}
