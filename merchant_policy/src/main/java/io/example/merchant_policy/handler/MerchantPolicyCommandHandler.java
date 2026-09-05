package io.example.merchant_policy.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_policy.service.MerchantPoliciesCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponseMerchantPolicies;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponseMerchantPoliciesDeleteAt;
import pb.merchant_policy.MerchantPolicyCommon.FindByIdMerchantPoliciesRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantPolicyCommandHandler
        implements pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.MerchantPolicyCommandServiceApi {
    private final MerchantPoliciesCommandService service;

    @Override
    public Future<ApiResponseMerchantPolicies> create(CreateMerchantPoliciesRequest req) {
        var domainReq = io.example.merchant_policy.domain.requests.CreateMerchantPoliciesRequest.builder()
                .merchantId(req.getMerchantId())
                .policyType(req.getPolicyType())
                .title(req.getTitle())
                .description(req.getDescription())
                .build();

        return service.create(domainReq)
                .map(data -> ApiResponseMerchantPolicies.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProto(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantPolicies> update(UpdateMerchantPoliciesRequest req) {
        var domainReq = io.example.merchant_policy.domain.requests.UpdateMerchantPoliciesRequest.builder()
                .merchantPolicyId(req.getMerchantPolicyId())
                .policyType(req.getPolicyType())
                .title(req.getTitle())
                .description(req.getDescription())
                .build();

        return service.update(domainReq)
                .map(data -> ApiResponseMerchantPolicies.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProto(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantPoliciesDeleteAt> trashedMerchantPolicies(FindByIdMerchantPoliciesRequest req) {
        return service.trash((long) req.getId())
                .map(data -> ApiResponseMerchantPoliciesDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProto(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantPoliciesDeleteAt> restoreMerchantPolicies(FindByIdMerchantPoliciesRequest req) {
        return service.restore((long) req.getId())
                .map(data -> ApiResponseMerchantPoliciesDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProto(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDelete> deleteMerchantPoliciesPermanent(FindByIdMerchantPoliciesRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(v -> ApiResponseMerchantDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Policy deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> restoreAllMerchantPolicies(Empty req) {
        return service.restoreAll()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All policies restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> deleteAllMerchantPoliciesPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All policies permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

  @Override
  public pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.MerchantPolicyCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.Update, this::update);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.TrashedMerchantPolicies, this::trashedMerchantPolicies);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.RestoreMerchantPolicies, this::restoreMerchantPolicies);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.DeleteMerchantPoliciesPermanent, this::deleteMerchantPoliciesPermanent);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.RestoreAllMerchantPolicies, this::restoreAllMerchantPolicies);
    GrpcServerBinder.bind(server, pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcServer.DeleteAllMerchantPoliciesPermanent, this::deleteAllMerchantPoliciesPermanent);
    return this;
  }
}