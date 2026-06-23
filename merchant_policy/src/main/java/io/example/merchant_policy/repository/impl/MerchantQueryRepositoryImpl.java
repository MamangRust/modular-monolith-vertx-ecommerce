package io.example.merchant_policy.repository.impl;

import io.example.merchant_policy.repository.MerchantQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

@RequiredArgsConstructor
public class MerchantQueryRepositoryImpl implements MerchantQueryRepository {
  private final VertxMerchantQueryServiceGrpcClient client;

  @Override
  public Future<Boolean> existsById(int userId) {
    FindByIdMerchantRequest request = FindByIdMerchantRequest.newBuilder()
        .setId(userId)
        .build();

    return client.findById(request)
        .map(resp -> resp != null && "success".equalsIgnoreCase(resp.getStatus()))
        .recover(err -> Future.succeededFuture(false));
  }
}
