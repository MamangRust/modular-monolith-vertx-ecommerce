package io.example.merchant.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest;

public interface MerchantDocumentQueryService {
  Future<ApiResponsePagination<List<MerchantDocumentResponse>>> getAllDocuments(FindAllMerchantDocumentsRequest req);
  Future<ApiResponsePagination<List<MerchantDocumentResponse>>> getActiveDocuments(FindAllMerchantDocumentsRequest req);
  Future<ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>>> getTrashedDocuments(FindAllMerchantDocumentsRequest req);
  Future<ApiResponse<MerchantDocumentResponse>> getDocumentById(Integer documentId);
}
