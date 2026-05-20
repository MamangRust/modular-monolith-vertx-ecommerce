package io.example.merchant_detail.service;

import io.example.common.domain.ApiResponse;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest;
import pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest;

public interface MerchantDetailCommandService {
  Future<ApiResponse<MerchantDetailResponse>> create(CreateMerchantDetailRequest req);
  Future<ApiResponse<MerchantDetailResponse>> update(UpdateMerchantDetailRequest req);
  Future<ApiResponse<MerchantDetailResponseDeleteAt>> trash(Long id);
  Future<ApiResponse<MerchantDetailResponseDeleteAt>> restore(Long id);
  Future<ApiResponse<Boolean>> deletePermanent(Long id);
  Future<ApiResponse<Integer>> restoreAll();
  Future<ApiResponse<Integer>> deleteAllPermanent();
}
