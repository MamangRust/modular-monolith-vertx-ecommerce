package io.example.merchant_business.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_business.domain.requests.FindAllMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusiness;
import io.vertx.core.Future;

public interface MerchantBusinessQueryRepository {
  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformation(FindAllMerchantBusinessRequest req);

  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationActive(FindAllMerchantBusinessRequest req);

  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationTrashed(FindAllMerchantBusinessRequest req);

  Future<MerchantBusiness> getMerchantBusinessInformation(Long id);

  Future<MerchantBusiness> findByTrashedId(Long id);
}
