package io.example.merchant_policy.handler;

import com.google.protobuf.Empty;
import io.example.merchant_policy.service.MerchantPoliciesCommandService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponseMerchantPolicies;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponseMerchantPoliciesDeleteAt;
import pb.merchant_policy.MerchantPolicyCommon.FindByIdMerchantPoliciesRequest;

public class MerchantPolicyCommandHandler implements pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.MerchantPolicyCommandServiceApi {
  private final MerchantPoliciesCommandService service;

  public MerchantPolicyCommandHandler(MerchantPoliciesCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantPolicies> create(CreateMerchantPoliciesRequest req) {
    return service.create(req)
        .map(resp -> {
          var builder = ApiResponseMerchantPolicies.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProto(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantPolicies> update(UpdateMerchantPoliciesRequest req) {
    return service.update(req)
        .map(resp -> {
          var builder = ApiResponseMerchantPolicies.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProto(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantPoliciesDeleteAt> trashedMerchantPolicies(FindByIdMerchantPoliciesRequest req) {
    return service.trash((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantPoliciesDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProto(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantPoliciesDeleteAt> restoreMerchantPolicies(FindByIdMerchantPoliciesRequest req) {
    return service.restore((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantPoliciesDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProto(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantPoliciesPermanent(FindByIdMerchantPoliciesRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(resp -> ApiResponseMerchantDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantPolicies(Empty req) {
    return service.restoreAll()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantPoliciesPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
