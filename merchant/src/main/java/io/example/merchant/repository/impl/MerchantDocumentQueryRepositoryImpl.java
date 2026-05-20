package io.example.merchant.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.merchant.model.MerchantDocument;
import io.example.merchant.repository.MerchantDocumentQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantDocumentQueryRepositoryImpl implements MerchantDocumentQueryRepository {
  private final Pool client;

  public MerchantDocumentQueryRepositoryImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<PagedResult<MerchantDocument>> getDocuments(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    return client
        .preparedQuery("""
            SELECT document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
            FROM merchant_documents
            WHERE ($1::TEXT IS NULL OR document_type ILIKE '%' || $1 || '%')
            ORDER BY created_at ASC LIMIT $2 OFFSET $3
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedDocuments);
  }

  @Override
  public Future<PagedResult<MerchantDocument>> getActiveDocuments(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    return client
        .preparedQuery("""
            SELECT document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
            FROM merchant_documents
            WHERE deleted_at IS NULL AND ($1::TEXT IS NULL OR document_type ILIKE '%' || $1 || '%')
            ORDER BY created_at ASC LIMIT $2 OFFSET $3
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedDocuments);
  }

  @Override
  public Future<PagedResult<MerchantDocument>> getTrashedDocuments(String search, int page, int pageSize) {
    int offset = (page > 0 ? page - 1 : 0) * pageSize;
    return client
        .preparedQuery("""
            SELECT document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at, COUNT(*) OVER() AS total_count
            FROM merchant_documents
            WHERE deleted_at IS NOT NULL AND ($1::TEXT IS NULL OR document_type ILIKE '%' || $1 || '%')
            ORDER BY deleted_at DESC LIMIT $2 OFFSET $3
            """)
        .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
        .map(this::mapPagedDocuments);
  }

  @Override
  public Future<MerchantDocument> getDocumentById(Integer documentId) {
    return client
        .preparedQuery("""
            SELECT document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            FROM merchant_documents
            WHERE document_id = $1 AND deleted_at IS NULL
            """)
        .execute(Tuple.of(documentId))
        .map(this::mapSingleOrNull);
  }

  private String normalizeSearch(String search) {
    return (search == null || search.isBlank()) ? null : search;
  }

  private MerchantDocument mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? MerchantDocument.fromRow(rows.iterator().next()) : null;
  }

  private PagedResult<MerchantDocument> mapPagedDocuments(RowSet<Row> rows) {
    List<MerchantDocument> docs = new ArrayList<>();
    int total = 0;
    for (Row row : rows) {
      docs.add(MerchantDocument.fromRow(row));
      if (total == 0) {
        Integer tc = row.getInteger("total_count");
        if (tc != null) total = tc;
      }
    }
    return new PagedResult<>(docs, total);
  }
}
