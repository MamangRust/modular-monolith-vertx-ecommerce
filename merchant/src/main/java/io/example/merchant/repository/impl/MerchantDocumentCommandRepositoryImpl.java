package io.example.merchant.repository.impl;

import io.example.merchant.model.MerchantDocument;
import io.example.merchant.repository.MerchantDocumentCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MerchantDocumentCommandRepositoryImpl implements MerchantDocumentCommandRepository {
  private final Pool client;

  public MerchantDocumentCommandRepositoryImpl(Pool client) {
    this.client = client;
  }

  @Override
  public Future<MerchantDocument> createDocument(Integer merchantId, String documentType, String documentUrl) {
    return client
        .preparedQuery("""
            INSERT INTO merchant_documents (merchant_id, document_type, document_url, status)
            VALUES ($1, $2, $3, 'pending')
            RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(merchantId, documentType, documentUrl))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> updateDocument(Integer documentId, Integer merchantId, String documentType, String documentUrl, String note, String status) {
    return client
        .preparedQuery("""
            UPDATE merchant_documents
            SET merchant_id = $1, document_type = $2, document_url = $3, note = $4, status = $5, updated_at = CURRENT_TIMESTAMP
            WHERE document_id = $6 AND deleted_at IS NULL
            RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(merchantId, documentType, documentUrl, note, status, documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> updateStatus(Integer documentId, String note, String status) {
    return client
        .preparedQuery("""
            UPDATE merchant_documents
            SET note = $1, status = $2, updated_at = CURRENT_TIMESTAMP
            WHERE document_id = $3 AND deleted_at IS NULL
            RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(note, status, documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> trashDocument(Integer documentId) {
    return client
        .preparedQuery("""
            UPDATE merchant_documents
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE document_id = $1 AND deleted_at IS NULL
            RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> restoreDocument(Integer documentId) {
    return client
        .preparedQuery("""
            UPDATE merchant_documents
            SET deleted_at = NULL
            WHERE document_id = $1 AND deleted_at IS NOT NULL
            RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
            """)
        .execute(Tuple.of(documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Void> deleteDocumentPermanently(Integer documentId) {
    return client
        .preparedQuery("DELETE FROM merchant_documents WHERE document_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(documentId))
        .mapEmpty();
  }

  @Override
  public Future<Void> restoreAllDocuments() {
    return client
        .query("UPDATE merchant_documents SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .mapEmpty();
  }

  @Override
  public Future<Void> deleteAllPermanentDocuments() {
    return client
        .query("DELETE FROM merchant_documents WHERE deleted_at IS NOT NULL")
        .execute()
        .mapEmpty();
  }

  private MerchantDocument mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? MerchantDocument.fromRow(rows.iterator().next()) : null;
  }
}
