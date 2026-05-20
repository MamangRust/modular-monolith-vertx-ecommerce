package io.example.merchant_detail.handler;

import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.vertx.core.Future;
import pb.MerchantSocialLinkCommon.ApiResponseMerchantSocial;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;

public class MerchantSocialCommandHandler implements pb.VertxMerchantSocialCommandServiceGrpcServer.MerchantSocialCommandServiceApi {
  private final MerchantSocialLinkCommandService service;

  public MerchantSocialCommandHandler(MerchantSocialLinkCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantSocial> create(CreateMerchantSocialRequest req) {
    return service.create(req)
        .map(resp -> {
          var builder = ApiResponseMerchantSocial.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoSocial(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantSocial> update(UpdateMerchantSocialRequest req) {
    return service.update(req)
        .map(resp -> {
          var builder = ApiResponseMerchantSocial.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoSocial(resp.data()));
          }
          return builder.build();
        });
  }
}
