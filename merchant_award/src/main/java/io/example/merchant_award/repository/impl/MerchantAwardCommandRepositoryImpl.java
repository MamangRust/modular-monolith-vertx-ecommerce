package io.example.merchant_award.repository.impl;

import java.time.LocalDate;

import io.example.merchant_award.domain.requests.CreateMerchantAwardRequest;
import io.example.merchant_award.domain.requests.UpdateMerchantAwardRequest;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.repository.MerchantAwardCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantAwardCommandRepositoryImpl implements MerchantAwardCommandRepository {
  private final Pool pool;

  @Override
  public Future<MerchantAward> create(CreateMerchantAwardRequest req) {
    LocalDate issueDate = parseLocalDate(req.getIssueDate());
    LocalDate expiryDate = parseLocalDate(req.getExpiryDate());

    return pool.preparedQuery("""
        INSERT INTO merchant_certifications_and_awards (
            merchant_id, title, description, issued_by, issue_date, expiry_date, certificate_url
        )
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        RETURNING *;
        """)
        .execute(Tuple.of(req.getMerchantId(), req.getTitle(), req.getDescription(),
            req.getIssuedBy(), issueDate, expiryDate, req.getCertificateUrl()))
        .map(rows -> {
          if (rows.iterator().hasNext()) {
            return MerchantAward.fromRow(rows.iterator().next());
          }
          return null;
        });
  }

  @Override
  public Future<MerchantAward> update(UpdateMerchantAwardRequest req) {
    LocalDate issueDate = parseLocalDate(req.getIssueDate());
    LocalDate expiryDate = parseLocalDate(req.getExpiryDate());

    return pool.preparedQuery("""
        UPDATE merchant_certifications_and_awards
        SET title = COALESCE(NULLIF($2, ''), title), description = COALESCE(NULLIF($3, ''), description), issued_by = COALESCE(NULLIF($4, ''), issued_by), issue_date = COALESCE(NULLIF($5::TEXT, ''), issue_date),
            expiry_date = COALESCE(NULLIF($6::TEXT, ''), expiry_date), certificate_url = COALESCE(NULLIF($7, ''), certificate_url), updated_at = CURRENT_TIMESTAMP
        WHERE merchant_certification_id = $1 AND deleted_at IS NULL
        RETURNING *;
        """)
        .execute(Tuple.of(req.getMerchantCertificationId(), req.getTitle() != null ? req.getTitle() : "", req.getDescription() != null ? req.getDescription() : "",
            req.getIssuedBy() != null ? req.getIssuedBy() : "", req.getIssueDate() != null ? req.getIssueDate() : "", req.getExpiryDate() != null ? req.getExpiryDate() : "", req.getCertificateUrl() != null ? req.getCertificateUrl() : ""))
        .map(rows -> {
          if (rows.iterator().hasNext()) {
            return MerchantAward.fromRow(rows.iterator().next());
          }
          return null;
        });
  }

  @Override
  public Future<MerchantAward> trash(Long id) {
    return pool.preparedQuery("""
        UPDATE merchant_certifications_and_awards
        SET deleted_at = CURRENT_TIMESTAMP
        WHERE merchant_certification_id = $1 AND deleted_at IS NULL
        RETURNING *;
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
  public Future<MerchantAward> restore(Long id) {
    return pool.preparedQuery("""
        UPDATE merchant_certifications_and_awards
        SET deleted_at = NULL
        WHERE merchant_certification_id = $1 AND deleted_at IS NOT NULL
        RETURNING *;
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
  public Future<Boolean> deletePermanent(Long id) {
    return pool.preparedQuery("""
        DELETE FROM merchant_certifications_and_awards
        WHERE merchant_certification_id = $1 AND deleted_at IS NOT NULL;
        """)
        .execute(Tuple.of(id))
        .map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Future<Integer> restoreAll() {
    return pool.preparedQuery("""
        UPDATE merchant_certifications_and_awards
        SET deleted_at = NULL
        WHERE deleted_at IS NOT NULL;
        """)
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAllPermanent() {
    return pool.preparedQuery("""
        DELETE FROM merchant_certifications_and_awards
        WHERE deleted_at IS NOT NULL;
        """)
        .execute()
        .map(RowSet::rowCount);
  }

  private LocalDate parseLocalDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      return null;
    }
  }
}
