package io.example.merchant_policy.handler;

import com.google.protobuf.StringValue;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponse;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponseDeleteAt;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;

public class ProtoConverter {

  public static pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse toProto(MerchantPoliciesResponse model) {
    if (model == null) {
      return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse.getDefaultInstance();
    }
    return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setPolicyType(model.getPolicyType())
        .setTitle(model.getTitle())
        .setDescription(model.getDescription())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt())
        .setMerchantName(model.getMerchantName() != null ? model.getMerchantName() : "")
        .build();
  }

  public static pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse toProto(MerchantPoliciesRelationResponse model) {
    if (model == null) {
      return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse.getDefaultInstance();
    }
    return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponse.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setPolicyType(model.getPolicyType())
        .setTitle(model.getTitle())
        .setDescription(model.getDescription())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt())
        .setMerchantName(model.getMerchantName() != null ? model.getMerchantName() : "")
        .build();
  }

  public static pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt toProto(MerchantPoliciesResponseDeleteAt model) {
    if (model == null) {
      return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt.getDefaultInstance();
    }
    var builder = pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setPolicyType(model.getPolicyType())
        .setTitle(model.getTitle())
        .setDescription(model.getDescription())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt())
        .setMerchantName(model.getMerchantName() != null ? model.getMerchantName() : "");

    if (model.getDeletedAt() != null && !model.getDeletedAt().isEmpty()) {
      builder.setDeletedAt(StringValue.of(model.getDeletedAt()));
    }
    return builder.build();
  }

  public static pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt toProto(MerchantPoliciesRelationResponseDeleteAt model) {
    if (model == null) {
      return pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt.getDefaultInstance();
    }
    var builder = pb.merchant_policy.MerchantPolicyCommon.MerchantPoliciesResponseDeleteAt.newBuilder()
        .setId(model.getId().intValue())
        .setMerchantId(model.getMerchantId())
        .setPolicyType(model.getPolicyType())
        .setTitle(model.getTitle())
        .setDescription(model.getDescription())
        .setCreatedAt(model.getCreatedAt())
        .setUpdatedAt(model.getUpdatedAt())
        .setMerchantName(model.getMerchantName() != null ? model.getMerchantName() : "");

    if (model.getDeletedAt() != null && !model.getDeletedAt().isEmpty()) {
      builder.setDeletedAt(StringValue.of(model.getDeletedAt()));
    }
    return builder.build();
  }
}
