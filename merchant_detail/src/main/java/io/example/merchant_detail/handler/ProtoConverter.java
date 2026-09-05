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
    var builder = pb.merchant_detail.MerchantDetailCommon.MerchantSocialMediaLinkResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantDetailId(model.getMerchantDetailId());
    if (model.getPlatform() != null) {
      builder.setPlatform(model.getPlatform());
    }
    if (model.getUrl() != null) {
      builder.setUrl(model.getUrl());
    }
    if (model.getCreatedAt() != null) {
      builder.setCreatedAt(model.getCreatedAt());
    }
    if (model.getUpdatedAt() != null) {
      builder.setUpdatedAt(model.getUpdatedAt());
    }
    return builder.build();
  }

  public static pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse toProtoResponse(MerchantDetailResponse model) {
    if (model == null) {
      return pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse.getDefaultInstance();
    }
    var builder = pb.merchant_detail.MerchantDetailCommon.MerchantDetailResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId());

    setIfPresent(builder::setDisplayName, model.getDisplayName());
    setIfPresent(builder::setCoverImageUrl, model.getCoverImageUrl());
    setIfPresent(builder::setLogoUrl, model.getLogoUrl());
    setIfPresent(builder::setShortDescription, model.getShortDescription());
    setIfPresent(builder::setWebsiteUrl, model.getWebsiteUrl());
    setIfPresent(builder::setCreatedAt, model.getCreatedAt());
    setIfPresent(builder::setUpdatedAt, model.getUpdatedAt());

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
        .setMerchantId(model.getMerchantId());

    setIfPresent(builder::setDisplayName, model.getDisplayName());
    setIfPresent(builder::setCoverImageUrl, model.getCoverImageUrl());
    setIfPresent(builder::setLogoUrl, model.getLogoUrl());
    setIfPresent(builder::setShortDescription, model.getShortDescription());
    setIfPresent(builder::setWebsiteUrl, model.getWebsiteUrl());
    setIfPresent(builder::setCreatedAt, model.getCreatedAt());
    setIfPresent(builder::setUpdatedAt, model.getUpdatedAt());

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
        .setMerchantId(model.getMerchantId());

    setIfPresent(builder::setDisplayName, model.getDisplayName());
    setIfPresent(builder::setCoverImageUrl, model.getCoverImageUrl());
    setIfPresent(builder::setLogoUrl, model.getLogoUrl());
    setIfPresent(builder::setShortDescription, model.getShortDescription());
    setIfPresent(builder::setWebsiteUrl, model.getWebsiteUrl());
    setIfPresent(builder::setCreatedAt, model.getCreatedAt());
    setIfPresent(builder::setUpdatedAt, model.getUpdatedAt());

    if (model.getSocialMediaLinks() != null) {
      for (var link : model.getSocialMediaLinks()) {
        builder.addSocialMediaLinks(toProtoSocial(link));
      }
    }
    return builder.build();
  }

  /** Calls the protobuf builder setter only when the value is non-null. */
  private static void setIfPresent(java.util.function.Consumer<String> setter, String value) {
    if (value != null) {
      setter.accept(value);
    }
  }
}
