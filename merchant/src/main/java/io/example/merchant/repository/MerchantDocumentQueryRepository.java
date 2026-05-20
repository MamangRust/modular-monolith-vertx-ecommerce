package io.example.merchant.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant.model.MerchantDocument;
import io.vertx.core.Future;

public interface MerchantDocumentQueryRepository {
  Future<PagedResult<MerchantDocument>> getDocuments(String search, int page, int pageSize);
  Future<PagedResult<MerchantDocument>> getActiveDocuments(String search, int page, int pageSize);
  Future<PagedResult<MerchantDocument>> getTrashedDocuments(String search, int page, int pageSize);
  Future<MerchantDocument> getDocumentById(Integer documentId);
}
