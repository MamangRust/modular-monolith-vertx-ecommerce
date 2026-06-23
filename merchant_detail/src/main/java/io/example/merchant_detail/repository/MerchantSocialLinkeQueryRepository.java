package io.example.merchant_detail.repository;

import io.example.merchant_detail.model.MerchantSocialMediaLink;
import io.vertx.core.Future;

public interface MerchantSocialLinkeQueryRepository {
    Future<MerchantSocialMediaLink> getByMerchantId(Integer merchantId);

    Future<MerchantSocialMediaLink> findByTrashedId(Integer socialLinkId);
}
