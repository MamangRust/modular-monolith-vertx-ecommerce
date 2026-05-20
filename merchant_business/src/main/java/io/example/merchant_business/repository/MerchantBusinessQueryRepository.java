package io.example.merchant_business.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_business.model.MerchantBusiness;
import io.vertx.core.Future;

public interface MerchantBusinessQueryRepository {
  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformation(String search, int page, int pageSize);
  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantBusiness>> getMerchantsBusinessInformationTrashed(String search, int page, int pageSize);
  Future<MerchantBusiness> getMerchantBusinessInformation(Long id);
}
