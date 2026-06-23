package io.example.merchant.repository;

import io.example.merchant.domain.requests.CreateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentStatusRequest;
import io.example.merchant.model.MerchantDocument;
import io.vertx.core.Future;

public interface MerchantDocumentCommandRepository {
  Future<MerchantDocument> createDocument(CreateMerchantDocumentRequest request);

  Future<MerchantDocument> updateDocument(UpdateMerchantDocumentRequest request);

  Future<MerchantDocument> updateStatus(UpdateMerchantDocumentStatusRequest request);

  Future<MerchantDocument> trashDocument(Long documentId);

  Future<MerchantDocument> restoreDocument(Long documentId);

  Future<Boolean> deleteDocumentPermanently(Long documentId);

  Future<Integer> restoreAllDocuments();

  Future<Integer> deleteAllPermanentDocuments();
}
