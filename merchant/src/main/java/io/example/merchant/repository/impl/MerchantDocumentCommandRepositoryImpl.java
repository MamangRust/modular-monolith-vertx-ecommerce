package io.example.merchant.repository.impl;

import io.example.merchant.domain.requests.CreateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentStatusRequest;
import io.example.merchant.model.MerchantDocument;
import io.example.merchant.repository.MerchantDocumentCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDocumentCommandRepositoryImpl implements MerchantDocumentCommandRepository {
  private final Pool client;

  @Override
  public Future<MerchantDocument> createDocument(CreateMerchantDocumentRequest request) {
    return client
        .preparedQuery(
            """
                INSERT INTO merchant_documents (merchant_id, document_type, document_url, status)
                VALUES ($1, $2, $3, 'pending')
                RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
                """)
        .execute(Tuple.of(request.getMerchantId(), request.getDocumentType(), request.getDocumentUrl()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> updateDocument(UpdateMerchantDocumentRequest request) {
    return client
        .preparedQuery(
            """
                UPDATE merchant_documents
                SET merchant_id = $1, document_type = $2, document_url = $3, note = $4, status = $5, updated_at = CURRENT_TIMESTAMP
                WHERE document_id = $6 AND deleted_at IS NULL
                RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
                """)
        .execute(Tuple.of(
            request.getMerchantId(),
            request.getDocumentType(),
            request.getDocumentUrl(),
            request.getNote(),
            request.getStatus(),
            request.getDocumentId()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> updateStatus(UpdateMerchantDocumentStatusRequest request) {
    return client
        .preparedQuery(
            """
                UPDATE merchant_documents
                SET note = $1, status = $2, updated_at = CURRENT_TIMESTAMP
                WHERE document_id = $3 AND deleted_at IS NULL
                RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
                """)
        .execute(Tuple.of(request.getNote(), request.getStatus(), request.getDocumentId()))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> trashDocument(Long documentId) {
    return client
        .preparedQuery(
            """
                UPDATE merchant_documents
                SET deleted_at = CURRENT_TIMESTAMP
                WHERE document_id = $1 AND deleted_at IS NULL
                RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
                """)
        .execute(Tuple.of(documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<MerchantDocument> restoreDocument(Long documentId) {
    return client
        .preparedQuery(
            """
                UPDATE merchant_documents
                SET deleted_at = NULL
                WHERE document_id = $1 AND deleted_at IS NOT NULL
                RETURNING document_id, merchant_id, document_type, document_url, status, note, uploaded_at, created_at, updated_at, deleted_at
                """)
        .execute(Tuple.of(documentId))
        .map(this::mapSingleOrNull);
  }

  @Override
  public Future<Boolean> deleteDocumentPermanently(Long documentId) {
    return client
        .preparedQuery("DELETE FROM merchant_documents WHERE document_id = $1 AND deleted_at IS NOT NULL")
        .execute(Tuple.of(documentId))
        .map(rows -> rows.rowCount() > 0);
  }

  @Override
  public Future<Integer> restoreAllDocuments() {
    return client
        .query("UPDATE merchant_documents SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  @Override
  public Future<Integer> deleteAllPermanentDocuments() {
    return client
        .query("DELETE FROM merchant_documents WHERE deleted_at IS NOT NULL")
        .execute()
        .map(RowSet::rowCount);
  }

  private MerchantDocument mapSingleOrNull(RowSet<Row> rows) {
    return rows.iterator().hasNext() ? MerchantDocument.fromRow(rows.iterator().next()) : null;
  }
}
