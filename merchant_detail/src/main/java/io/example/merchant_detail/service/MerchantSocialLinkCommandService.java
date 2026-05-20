package io.example.merchant_detail.service;

import io.example.common.domain.ApiResponse;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;
import io.vertx.core.Future;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;

public interface MerchantSocialLinkCommandService {
  Future<ApiResponse<MerchantSocialMediaLinkResponse>> create(CreateMerchantSocialRequest req);
  Future<ApiResponse<MerchantSocialMediaLinkResponse>> update(UpdateMerchantSocialRequest req);
  Future<ApiResponse<Boolean>> trash(Long id);
  Future<ApiResponse<Boolean>> restore(Long id);
  Future<ApiResponse<Boolean>> deletePermanent(Long id);
  Future<ApiResponse<Integer>> restoreAll();
  Future<ApiResponse<Integer>> deleteAllPermanent();
}
