package io.example.merchant_detail.handler;

import com.google.protobuf.StringValue;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;

public class ProtoConverter {

  public static pb.merchant_detail.MerchantDetailCommon.MerchantSocialMediaLinkResponse toProtoSocial(MerchantSocialMediaLinkResponse model) {
    if (model == null) {
      return pb.merchant_detail.MerchantDetailCommon.MerchantSocialMediaLinkResponse.getDefaultInstance();
    }
    return pb.merchant_detail.MerchantDetailCommon.MerchantSocialMediaLinkResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantDetailId(model.getMerchantDetailId())
        .setPlatform(model.getPlatform())
        .setUrl(model.getUrl())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt())
        .build();
  }

  public static pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse toProtoResponse(MerchantDetailResponse model) {
    if (model == null) {
      return pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse.getDefaultInstance();
    }
    var builder = pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setDisplayName(model.getDisplayName())
        .setCoverImageUrl(model.getCoverImageUrl())
        .setLogoUrl(model.getLogoUrl())
        .setShortDescription(model.getShortDescription())
        .setWebsiteUrl(model.getWebsiteUrl())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt());

    if (model.getSocialMediaLinks() != null) {
      for (var link : model.getSocialMediaLinks()) {
        builder.addSocialMediaLinks(toProtoSocial(link));
      }
    }
    return builder.build();
  }

  public static pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt toProtoResponseDeleteAt(MerchantDetailResponseDeleteAt model) {
    if (model == null) {
      return pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt.getDefaultInstance();
    }
    var builder = pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setDisplayName(model.getDisplayName())
        .setCoverImageUrl(model.getCoverImageUrl())
        .setLogoUrl(model.getLogoUrl())
        .setShortDescription(model.getShortDescription())
        .setWebsiteUrl(model.getWebsiteUrl())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt());

    if (model.getDeletedAt() != null && !model.getDeletedAt().isEmpty()) {
      builder.setDeletedAt(StringValue.of(model.getDeletedAt()));
    }

    if (model.getSocialMediaLinks() != null) {
      for (var link : model.getSocialMediaLinks()) {
        builder.addSocialMediaLinks(toProtoSocial(link));
      }
    }
    return builder.build();
  }

  public static pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt toProtoResponseDeleteAt(MerchantDetailResponse model) {
    if (model == null) {
      return pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt.getDefaultInstance();
    }
    var builder = pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponseDeleteAt.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setDisplayName(model.getDisplayName())
        .setCoverImageUrl(model.getCoverImageUrl())
        .setLogoUrl(model.getLogoUrl())
        .setShortDescription(model.getShortDescription())
        .setWebsiteUrl(model.getWebsiteUrl())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt());

    if (model.getSocialMediaLinks() != null) {
      for (var link : model.getSocialMediaLinks()) {
        builder.addSocialMediaLinks(toProtoSocial(link));
      }
    }
    return builder.build();
  }
}
