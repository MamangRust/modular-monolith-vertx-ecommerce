package io.example.merchant_award.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_award.domain.requests.FindAllMerchantAwardsRequest;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantAwardQueryRepositoryImpl implements MerchantAwardQueryRepository {
  private final Pool pool;

  @Override
  public Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwards(FindAllMerchantAwardsRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    int offset = (page - 1) * pageSize;
    String searchVal = req.getSearch() != null ? req.getSearch() : "";

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
  public Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwardsActive(FindAllMerchantAwardsRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    int offset = (page - 1) * pageSize;
    String searchVal = req.getSearch() != null ? req.getSearch() : "";

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
  public Future<PagedResult<MerchantAward>> getTrashedCertificationsAndAwards(FindAllMerchantAwardsRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    int offset = (page - 1) * pageSize;
    String searchVal = req.getSearch() != null ? req.getSearch() : "";

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

  @Override
  public Future<MerchantAward> findByTrashedId(Long id) {
    return pool.preparedQuery("""
        SELECT
            merchant_certification_id, merchant_id, title, description,
            issued_by, issue_date, expiry_date, certificate_url,
            created_at, updated_at, deleted_at
        FROM merchant_certifications_and_awards
        WHERE merchant_certification_id = $1
          AND deleted_at IS NOT NULL;
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
