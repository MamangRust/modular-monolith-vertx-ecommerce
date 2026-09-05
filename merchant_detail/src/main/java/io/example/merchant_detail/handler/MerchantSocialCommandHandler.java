package io.example.merchant_detail.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.MerchantSocialLinkCommon.ApiResponseMerchantSocial;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantSocialCommandHandler
    implements pb.VertxMerchantSocialCommandServiceGrpcServer.MerchantSocialCommandServiceApi {

  private final MerchantSocialLinkCommandService service;

  @Override
  public Future<ApiResponseMerchantSocial> create(CreateMerchantSocialRequest req) {
    var domainReq = io.example.merchant_detail.domain.requests.CreateMerchantSocialRequest.builder()
        .merchantDetailId(req.getMerchantDetailId())
        .platform(req.getPlatform())
        .url(req.getUrl())
        .build();

    return service.create(domainReq)
        .map(data -> ApiResponseMerchantSocial.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoSocial(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantSocial> update(UpdateMerchantSocialRequest req) {
    var domainReq = io.example.merchant_detail.domain.requests.UpdateMerchantSocialRequest.builder()
        .id(req.getId())
        .merchantDetailId(req.getMerchantDetailId())
        .platform(req.getPlatform())
        .url(req.getUrl())
        .build();

    return service.update(domainReq)
        .map(data -> ApiResponseMerchantSocial.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoSocial(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.VertxMerchantSocialCommandServiceGrpcServer.MerchantSocialCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.VertxMerchantSocialCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.VertxMerchantSocialCommandServiceGrpcServer.Update, this::update);
    return this;
  }
}