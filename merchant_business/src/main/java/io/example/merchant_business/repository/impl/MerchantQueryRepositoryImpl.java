package io.example.merchant_business.repository.impl;

import io.example.merchant_business.repository.MerchantQueryRepository;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

public class MerchantQueryRepositoryImpl implements MerchantQueryRepository {
  private final VertxMerchantQueryServiceGrpcClient client;

  public MerchantQueryRepositoryImpl(VertxMerchantQueryServiceGrpcClient client) {
    this.client = client;
  }

  @Override
  public Future<Boolean> findById(Integer id) {
    FindByIdMerchantRequest request = FindByIdMerchantRequest.newBuilder()
        .setId(id)
        .build();

    return client.findById(request)
        .map(resp -> resp != null && "success".equalsIgnoreCase(resp.getStatus()) && resp.hasData());
  }
}
