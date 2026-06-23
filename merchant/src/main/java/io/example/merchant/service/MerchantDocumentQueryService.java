package io.example.merchant.service;

import io.example.common.domain.PagedResult;
import io.example.merchant.domain.requests.FindAllMerchantDocumentsRequest;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantDocumentQueryService {
  Future<PagedResult<MerchantDocumentResponse>> getAllDocuments(FindAllMerchantDocumentsRequest req);

  Future<PagedResult<MerchantDocumentResponse>> getActiveDocuments(FindAllMerchantDocumentsRequest req);

  Future<PagedResult<MerchantDocumentResponseDeleteAt>> getTrashedDocuments(FindAllMerchantDocumentsRequest req);

  Future<MerchantDocumentResponse> getDocumentById(Long documentId);
}