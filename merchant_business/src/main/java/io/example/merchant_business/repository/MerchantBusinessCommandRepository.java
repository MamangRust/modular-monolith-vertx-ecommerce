package io.example.merchant_business.repository;

import io.example.merchant_business.model.MerchantBusiness;
import io.vertx.core.Future;
import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;

public interface MerchantBusinessCommandRepository {
  Future<MerchantBusiness> create(CreateMerchantBusinessRequest req);
  Future<MerchantBusiness> update(UpdateMerchantBusinessRequest req);
  Future<MerchantBusiness> trash(Long id);
  Future<MerchantBusiness> restore(Long id);
  Future<Boolean> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAllPermanent();
}
