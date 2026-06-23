package io.example.merchant_detail.repository;

import io.example.merchant_detail.domain.requests.CreateMerchantSocialRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantSocialRequest;
import io.example.merchant_detail.model.MerchantSocialMediaLink;
import io.vertx.core.Future;

public interface MerchantSocialLinkCommandRepository {
  Future<MerchantSocialMediaLink> create(CreateMerchantSocialRequest req);

  Future<MerchantSocialMediaLink> update(UpdateMerchantSocialRequest req);

  Future<MerchantSocialMediaLink> trash(Integer id);

  Future<MerchantSocialMediaLink> restore(Integer id);

  Future<Void> deletePermanent(Integer id);

  Future<Integer> restoreAll();

  Future<Integer> deleteAll();
}
