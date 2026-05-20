package io.example.merchant.service;

import io.example.common.model.ApiResponse;
import io.example.merchant.model.MerchantDocumentResponse;
import io.vertx.core.Future;
import pb.merchant_document.MerchantDocumentCommand.CreateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentStatusRequest;

public interface MerchantDocumentCommandService {
  Future<ApiResponse<MerchantDocumentResponse>> createDocument(CreateMerchantDocumentRequest req);
  Future<ApiResponse<MerchantDocumentResponse>> updateDocument(UpdateMerchantDocumentRequest req);
  Future<ApiResponse<MerchantDocumentResponse>> updateStatus(UpdateMerchantDocumentStatusRequest req);
  Future<ApiResponse<MerchantDocumentResponse>> trashDocument(Integer documentId);
  Future<ApiResponse<MerchantDocumentResponse>> restoreDocument(Integer documentId);
  Future<ApiResponse<Void>> deleteDocumentPermanently(Integer documentId);
  Future<ApiResponse<Void>> restoreAllDocuments();
  Future<ApiResponse<Void>> deleteAllPermanentDocuments();
}
