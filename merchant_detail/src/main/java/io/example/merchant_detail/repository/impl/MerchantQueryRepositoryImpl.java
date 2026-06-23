package io.example.merchant_detail.repository.impl;

import io.example.merchant_detail.repository.MerchantQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

@RequiredArgsConstructor
public class MerchantQueryRepositoryImpl implements MerchantQueryRepository {
  private final VertxMerchantQueryServiceGrpcClient client;

  @Override
  public Future<Boolean> findById(Integer merchantId) {
    FindByIdMerchantRequest request = FindByIdMerchantRequest.newBuilder()
        .setId(merchantId)
        .build();

    return client.findById(request)
        .map(response -> response != null && response.hasData());
  }
}
