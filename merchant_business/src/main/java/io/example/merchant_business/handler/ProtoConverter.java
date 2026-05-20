package io.example.merchant_business.handler;

import com.google.protobuf.StringValue;

import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;

public class ProtoConverter {

  public static pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponse toProtoResponse(MerchantBusinessResponse r) {
    if (r == null) {
      return pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponse.getDefaultInstance();
    }
    return pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponse.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setBusinessType(r.getBusinessType() != null ? r.getBusinessType() : "")
        .setTaxId(r.getTaxId() != null ? r.getTaxId() : "")
        .setEstablishedYear(r.getEstablishedYear() != null ? r.getEstablishedYear() : 0)
        .setNumberOfEmployees(r.getNumberOfEmployees() != null ? r.getNumberOfEmployees() : 0)
        .setWebsiteUrl(r.getWebsiteUrl() != null ? r.getWebsiteUrl() : "")
        .setMerchantName(r.getMerchantName() != null ? r.getMerchantName() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .build();
  }

  public static pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponseDeleteAt toProtoResponseDeleteAt(MerchantBusinessResponseDeleteAt r) {
    if (r == null) {
      return pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponseDeleteAt.getDefaultInstance();
    }
    pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponseDeleteAt.Builder builder = pb.merchant_business.MerchantBusinessCommon.MerchantBusinessResponseDeleteAt.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setBusinessType(r.getBusinessType() != null ? r.getBusinessType() : "")
        .setTaxId(r.getTaxId() != null ? r.getTaxId() : "")
        .setEstablishedYear(r.getEstablishedYear() != null ? r.getEstablishedYear() : 0)
        .setNumberOfEmployees(r.getNumberOfEmployees() != null ? r.getNumberOfEmployees() : 0)
        .setWebsiteUrl(r.getWebsiteUrl() != null ? r.getWebsiteUrl() : "")
        .setMerchantName(r.getMerchantName() != null ? r.getMerchantName() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

    if (r.getDeletedAt() != null) {
      builder.setDeletedAt(StringValue.of(r.getDeletedAt()));
    }
    return builder.build();
  }
}
