package io.example.merchant_award.handler;

import com.google.protobuf.StringValue;

import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;

public class ProtoConverter {

  public static pb.merchant_award.MerchantAwardCommon.MerchantAwardResponse toProtoResponse(MerchantAwardResponse r) {
    if (r == null) {
      return pb.merchant_award.MerchantAwardCommon.MerchantAwardResponse.getDefaultInstance();
    }
    return pb.merchant_award.MerchantAwardCommon.MerchantAwardResponse.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setTitle(r.getTitle() != null ? r.getTitle() : "")
        .setDescription(r.getDescription() != null ? r.getDescription() : "")
        .setIssuedBy(r.getIssuedBy() != null ? r.getIssuedBy() : "")
        .setIssueDate(r.getIssueDate() != null ? r.getIssueDate() : "")
        .setExpiryDate(r.getExpiryDate() != null ? r.getExpiryDate() : "")
        .setCertificateUrl(r.getCertificateUrl() != null ? r.getCertificateUrl() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .setMerchantName(r.getMerchantName() != null ? r.getMerchantName() : "")
        .build();
  }

  public static pb.merchant_award.MerchantAwardCommon.MerchantAwardResponseDeleteAt toProtoResponseDeleteAt(MerchantAwardResponseDeleteAt r) {
    if (r == null) {
      return pb.merchant_award.MerchantAwardCommon.MerchantAwardResponseDeleteAt.getDefaultInstance();
    }
    pb.merchant_award.MerchantAwardCommon.MerchantAwardResponseDeleteAt.Builder builder = pb.merchant_award.MerchantAwardCommon.MerchantAwardResponseDeleteAt.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setTitle(r.getTitle() != null ? r.getTitle() : "")
        .setDescription(r.getDescription() != null ? r.getDescription() : "")
        .setIssuedBy(r.getIssuedBy() != null ? r.getIssuedBy() : "")
        .setIssueDate(r.getIssueDate() != null ? r.getIssueDate() : "")
        .setExpiryDate(r.getExpiryDate() != null ? r.getExpiryDate() : "")
        .setCertificateUrl(r.getCertificateUrl() != null ? r.getCertificateUrl() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .setMerchantName(r.getMerchantName() != null ? r.getMerchantName() : "");

    if (r.getDeletedAt() != null) {
      builder.setDeletedAt(StringValue.of(r.getDeletedAt()));
    }
    return builder.build();
  }
}
