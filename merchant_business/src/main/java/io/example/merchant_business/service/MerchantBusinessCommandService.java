package io.example.merchant_business.service;

import io.example.common.model.ApiResponse;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;

public interface MerchantBusinessCommandService {
  Future<ApiResponse<MerchantBusinessResponse>> create(CreateMerchantBusinessRequest req);
  Future<ApiResponse<MerchantBusinessResponse>> update(UpdateMerchantBusinessRequest req);
  Future<ApiResponse<MerchantBusinessResponseDeleteAt>> trash(Long id);
  Future<ApiResponse<MerchantBusinessResponseDeleteAt>> restore(Long id);
  Future<ApiResponse<Boolean>> deletePermanent(Long id);
  Future<ApiResponse<Integer>> restoreAll();
  Future<ApiResponse<Integer>> deleteAllPermanent();
}
