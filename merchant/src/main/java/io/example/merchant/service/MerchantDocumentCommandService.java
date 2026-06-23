package io.example.merchant.service;

import io.example.merchant.domain.requests.CreateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentStatusRequest;
import io.example.merchant.model.MerchantDocumentResponse;
import io.vertx.core.Future;

public interface MerchantDocumentCommandService {
  Future<MerchantDocumentResponse> createDocument(CreateMerchantDocumentRequest req);

  Future<MerchantDocumentResponse> updateDocument(UpdateMerchantDocumentRequest req);

  Future<MerchantDocumentResponse> updateStatus(UpdateMerchantDocumentStatusRequest req);

  Future<MerchantDocumentResponse> trashDocument(Long documentId);

  Future<MerchantDocumentResponse> restoreDocument(Long documentId);

  Future<Void> deleteDocumentPermanently(Long documentId);

  Future<Void> restoreAllDocuments();

  Future<Void> deleteAllPermanentDocuments();
}