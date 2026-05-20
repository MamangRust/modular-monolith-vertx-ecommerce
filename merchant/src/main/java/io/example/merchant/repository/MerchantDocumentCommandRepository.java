package io.example.merchant.repository;

import io.example.merchant.model.MerchantDocument;
import io.vertx.core.Future;

public interface MerchantDocumentCommandRepository {
  Future<MerchantDocument> createDocument(Integer merchantId, String documentType, String documentUrl);
  Future<MerchantDocument> updateDocument(Integer documentId, Integer merchantId, String documentType, String documentUrl, String note, String status);
  Future<MerchantDocument> updateStatus(Integer documentId, String note, String status);
  Future<MerchantDocument> trashDocument(Integer documentId);
  Future<MerchantDocument> restoreDocument(Integer documentId);
  Future<Void> deleteDocumentPermanently(Integer documentId);
  Future<Void> restoreAllDocuments();
  Future<Void> deleteAllPermanentDocuments();
}
