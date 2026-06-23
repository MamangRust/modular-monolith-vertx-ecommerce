package io.example.merchant_business.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant_business.domain.requests.FindAllMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusiness;
import io.example.merchant_business.repository.MerchantBusinessQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantBusinessQueryRepositoryImpl implements MerchantBusinessQueryRepository {
  private final Pool client;

  @Override
  public Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformation(FindAllMerchantBusinessRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    String searchPattern = "%" + (req.getSearch() != null ? req.getSearch() : "") + "%";

    return client
        .preparedQuery("""
            SELECT
                mbi.merchant_business_info_id,
                mbi.merchant_id,
                mbi.business_type,
                mbi.tax_id,
                mbi.established_year,
                mbi.number_of_employees,
                mbi.website_url,
                mbi.created_at,
                mbi.updated_at,
                mbi.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_business_information mbi
            JOIN merchants m ON mbi.merchant_id = m.merchant_id
            WHERE LOWER(m.name) LIKE LOWER($1)
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
        .map(this::mapPagedResults);
  }

  @Override
  public Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationActive(
      FindAllMerchantBusinessRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    String searchPattern = "%" + (req.getSearch() != null ? req.getSearch() : "") + "%";

    return client
        .preparedQuery("""
            SELECT
                mbi.merchant_business_info_id,
                mbi.merchant_id,
                mbi.business_type,
                mbi.tax_id,
                mbi.established_year,
                mbi.number_of_employees,
                mbi.website_url,
                mbi.created_at,
                mbi.updated_at,
                mbi.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_business_information mbi
            JOIN merchants m ON mbi.merchant_id = m.merchant_id
            WHERE mbi.deleted_at IS NULL
              AND m.deleted_at IS NULL
              AND LOWER(m.name) LIKE LOWER($1)
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
        .map(this::mapPagedResults);
  }

  @Override
  public Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationTrashed(
      FindAllMerchantBusinessRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
    String searchPattern = "%" + (req.getSearch() != null ? req.getSearch() : "") + "%";

    return client
        .preparedQuery("""
            SELECT
                mbi.merchant_business_info_id,
                mbi.merchant_id,
                mbi.business_type,
                mbi.tax_id,
                mbi.established_year,
                mbi.number_of_employees,
                mbi.website_url,
                mbi.created_at,
                mbi.updated_at,
                mbi.deleted_at,
                m.name AS merchant_name,
                COUNT(*) OVER () AS total_count
            FROM merchant_business_information mbi
            JOIN merchants m ON mbi.merchant_id = m.merchant_id
            WHERE mbi.deleted_at IS NOT NULL
              AND m.deleted_at IS NULL
              AND LOWER(m.name) LIKE LOWER($1)
            LIMIT $2 OFFSET $3;
            """)
        .execute(Tuple.of(searchPattern, req.getPageSize(), offset))
        .map(this::mapPagedResults);
  }

  @Override
  public Future<MerchantBusiness> getMerchantBusinessInformation(Long id) {
    return client
        .preparedQuery("""
            SELECT
                mbi.merchant_business_info_id,
                mbi.merchant_id,
                mbi.business_type,
                mbi.tax_id,
                mbi.established_year,
                mbi.number_of_employees,
                mbi.website_url,
                mbi.created_at,
                mbi.updated_at,
                mbi.deleted_at,
                m.name AS merchant_name
            FROM merchant_business_information mbi
            JOIN merchants m ON mbi.merchant_id = m.merchant_id
            WHERE mbi.merchant_business_info_id = $1
              AND mbi.deleted_at IS NULL;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantBusiness> findByTrashedId(Long id) {
    return client
        .preparedQuery("""
            SELECT
                merchant_business_info_id,
                merchant_id,
                business_type,
                tax_id,
                established_year,
                number_of_employees,
                website_url,
                created_at,
                updated_at,
                deleted_at
            FROM merchant_business_information
            WHERE merchant_business_info_id = $1
              AND deleted_at IS NOT NULL;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  private PagedResult<MerchantBusiness> mapPagedResults(RowSet<Row> rows) {
    List<MerchantBusiness> list = new ArrayList<>();
    int total = 0;
    for (Row row : rows) {
      list.add(MerchantBusiness.fromRow(row));
      if (total == 0) {
        Integer tc = row.getInteger("total_count");
        if (tc != null) {
          total = tc;
        }
      }
    }
    return new PagedResult<>(list, total);
  }
}
