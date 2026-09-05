package io.example.merchant_business.repository.impl;

import io.example.merchant_business.model.MerchantBusiness;
import io.example.merchant_business.repository.MerchantBusinessCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import io.example.merchant_business.domain.requests.CreateMerchantBusinessRequest;
import io.example.merchant_business.domain.requests.UpdateMerchantBusinessRequest;

@RequiredArgsConstructor
public class MerchantBusinessCommandRepositoryImpl implements MerchantBusinessCommandRepository {
  private final Pool client;


  @Override
  public Future<MerchantBusiness> create(CreateMerchantBusinessRequest req) {
    return client
        .preparedQuery("""
            INSERT INTO merchant_business_information (
                merchant_id, business_type, tax_id, established_year, number_of_employees, website_url
            )
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING *;
            """)
        .execute(Tuple.of(
            req.getMerchantId(),
            req.getBusinessType(),
            req.getTaxId(),
            req.getEstablishedYear(),
            req.getNumberOfEmployees(),
            req.getWebsiteUrl()))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantBusiness> update(UpdateMerchantBusinessRequest req) {
    return client
        .preparedQuery("""
            UPDATE merchant_business_information
            SET business_type = COALESCE(NULLIF($2, ''), business_type), tax_id = COALESCE(NULLIF($3, ''), tax_id), established_year = COALESCE(NULLIF($4, ''), established_year),
                number_of_employees = COALESCE(NULLIF($5, ''), number_of_employees), website_url = COALESCE(NULLIF($6, ''), website_url), updated_at = CURRENT_TIMESTAMP
            WHERE merchant_business_info_id = $1 AND deleted_at IS NULL
            RETURNING *;
            """)
        .execute(Tuple.of(
            (long) req.getMerchantBusinessInfoId(),
            req.getBusinessType() != null ? req.getBusinessType() : "",
            req.getTaxId() != null ? req.getTaxId() : "",
            req.getEstablishedYear() != null ? req.getEstablishedYear() : "",
            req.getNumberOfEmployees() != null ? req.getNumberOfEmployees() : "",
            req.getWebsiteUrl() != null ? req.getWebsiteUrl() : ""))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantBusiness> trash(Long id) {
    return client
        .preparedQuery("""
            UPDATE merchant_business_information
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE merchant_business_info_id = $1 AND deleted_at IS NULL
            RETURNING *;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<MerchantBusiness> restore(Long id) {
    return client
        .preparedQuery("""
            UPDATE merchant_business_information
            SET deleted_at = NULL
            WHERE merchant_business_info_id = $1 AND deleted_at IS NOT NULL
            RETURNING *;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.iterator().hasNext() ? MerchantBusiness.fromRow(rows.iterator().next()) : null);
  }

  @Override
  public Future<Boolean> deletePermanent(Long id) {
    return client
        .preparedQuery("""
            DELETE FROM merchant_business_information
            WHERE merchant_business_info_id = $1 AND deleted_at IS NOT NULL;
            """)
        .execute(Tuple.of(id))
        .map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Future<Integer> restoreAll() {
    return client
        .preparedQuery("""
            UPDATE merchant_business_information
            SET deleted_at = NULL
            WHERE deleted_at IS NOT NULL;
            """)
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAllPermanent() {
    return client
        .preparedQuery("""
            DELETE FROM merchant_business_information
            WHERE deleted_at IS NOT NULL;
            """)
        .execute()
        .map(RowSet::rowCount);
  }
}
