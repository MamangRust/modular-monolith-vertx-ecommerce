package io.example.merchant.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant.domain.requests.FindAllMerchantDocumentsRequest;
import io.example.merchant.model.MerchantDocument;
import io.vertx.core.Future;

public interface MerchantDocumentQueryRepository {
  Future<PagedResult<MerchantDocument>> getDocuments(FindAllMerchantDocumentsRequest req);

  Future<PagedResult<MerchantDocument>> getActiveDocuments(FindAllMerchantDocumentsRequest req);

  Future<PagedResult<MerchantDocument>> getTrashedDocuments(FindAllMerchantDocumentsRequest req);

  Future<MerchantDocument> getDocumentById(Long documentId);

  Future<MerchantDocument> findByTrashedId(Long documentId);
}
