package io.example.merchant_detail.repository;

import io.example.merchant_detail.model.MerchantSocialMediaLink;
import io.vertx.core.Future;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;

public interface MerchantSocialLinkCommandRepository {
  Future<MerchantSocialMediaLink> create(CreateMerchantSocialRequest req);
  Future<MerchantSocialMediaLink> update(UpdateMerchantSocialRequest req);
  Future<MerchantSocialMediaLink> trash(Long id);
  Future<MerchantSocialMediaLink> restore(Long id);
  Future<Void> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAll();
}
