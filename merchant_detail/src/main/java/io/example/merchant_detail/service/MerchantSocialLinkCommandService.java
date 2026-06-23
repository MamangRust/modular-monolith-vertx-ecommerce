package io.example.merchant_detail.service;

import io.example.merchant_detail.domain.requests.CreateMerchantSocialRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantSocialRequest;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantSocialLinkCommandService {
  Future<MerchantSocialMediaLinkResponse> create(CreateMerchantSocialRequest req);

  Future<MerchantSocialMediaLinkResponse> update(UpdateMerchantSocialRequest req);

  Future<MerchantSocialMediaLinkResponseDeleteAt> trash(Integer id);

  Future<MerchantSocialMediaLinkResponseDeleteAt> restore(Integer id);

  Future<Void> deletePermanent(Integer id);

  Future<Void> restoreAll();

  Future<Void> deleteAllPermanent();
}