package io.example.merchant.handler;

import com.google.protobuf.StringValue;

import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;

public class ProtoConverter {

  public static pb.merchant.MerchantCommon.MerchantResponse fromMerchantResponse(MerchantResponse r) {
    if (r == null) {
      return pb.merchant.MerchantCommon.MerchantResponse.getDefaultInstance();
    }
    return pb.merchant.MerchantCommon.MerchantResponse.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setUserId(r.getUserId() != null ? r.getUserId() : 0)
        .setName(r.getName() != null ? r.getName() : "")
        .setDescription(r.getDescription() != null ? r.getDescription() : "")
        .setAddress(r.getAddress() != null ? r.getAddress() : "")
        .setContactEmail(r.getContactEmail() != null ? r.getContactEmail() : "")
        .setContactPhone(r.getContactPhone() != null ? r.getContactPhone() : "")
        .setStatus(r.getStatus() != null ? r.getStatus() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .build();
  }

  public static pb.merchant.MerchantCommon.MerchantResponseDeleteAt fromMerchantResponseDeleteAt(MerchantResponseDeleteAt r) {
    if (r == null) {
      return pb.merchant.MerchantCommon.MerchantResponseDeleteAt.getDefaultInstance();
    }
    pb.merchant.MerchantCommon.MerchantResponseDeleteAt.Builder b = pb.merchant.MerchantCommon.MerchantResponseDeleteAt.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setUserId(r.getUserId() != null ? r.getUserId() : 0)
        .setName(r.getName() != null ? r.getName() : "")
        .setDescription(r.getDescription() != null ? r.getDescription() : "")
        .setAddress(r.getAddress() != null ? r.getAddress() : "")
        .setContactEmail(r.getContactEmail() != null ? r.getContactEmail() : "")
        .setContactPhone(r.getContactPhone() != null ? r.getContactPhone() : "")
        .setStatus(r.getStatus() != null ? r.getStatus() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

    if (r.getDeletedAt() != null) {
      b.setDeletedAt(StringValue.of(r.getDeletedAt()));
    }
    return b.build();
  }

  public static pb.merchant.MerchantCommon.MerchantResponse fromMerchantResponseDeleteAtToResponse(MerchantResponseDeleteAt r) {
    if (r == null) {
      return pb.merchant.MerchantCommon.MerchantResponse.getDefaultInstance();
    }
    return pb.merchant.MerchantCommon.MerchantResponse.newBuilder()
        .setId(r.getId() != null ? r.getId() : 0)
        .setUserId(r.getUserId() != null ? r.getUserId() : 0)
        .setName(r.getName() != null ? r.getName() : "")
        .setDescription(r.getDescription() != null ? r.getDescription() : "")
        .setAddress(r.getAddress() != null ? r.getAddress() : "")
        .setContactEmail(r.getContactEmail() != null ? r.getContactEmail() : "")
        .setContactPhone(r.getContactPhone() != null ? r.getContactPhone() : "")
        .setStatus(r.getStatus() != null ? r.getStatus() : "")
        .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .build();
  }

  public static pb.merchant_document.MerchantDocumentCommon.MerchantDocument fromDocumentResponse(MerchantDocumentResponse r) {
    if (r == null) {
      return pb.merchant_document.MerchantDocumentCommon.MerchantDocument.getDefaultInstance();
    }
    return pb.merchant_document.MerchantDocumentCommon.MerchantDocument.newBuilder()
        .setDocumentId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setDocumentType(r.getDocumentType() != null ? r.getDocumentType() : "")
        .setDocumentUrl(r.getDocumentUrl() != null ? r.getDocumentUrl() : "")
        .setStatus(r.getStatus() != null ? r.getStatus() : "")
        .setNote(r.getNote() != null ? r.getNote() : "")
        .setUploadedAt(r.getUploadedAt() != null ? r.getUploadedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
        .build();
  }

  public static pb.merchant_document.MerchantDocumentCommon.MerchantDocumentDeleteAt fromDocumentResponseDeleteAt(MerchantDocumentResponseDeleteAt r) {
    if (r == null) {
      return pb.merchant_document.MerchantDocumentCommon.MerchantDocumentDeleteAt.getDefaultInstance();
    }
    pb.merchant_document.MerchantDocumentCommon.MerchantDocumentDeleteAt.Builder b = pb.merchant_document.MerchantDocumentCommon.MerchantDocumentDeleteAt.newBuilder()
        .setDocumentId(r.getId() != null ? r.getId() : 0)
        .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
        .setDocumentType(r.getDocumentType() != null ? r.getDocumentType() : "")
        .setDocumentUrl(r.getDocumentUrl() != null ? r.getDocumentUrl() : "")
        .setStatus(r.getStatus() != null ? r.getStatus() : "")
        .setNote(r.getNote() != null ? r.getNote() : "")
        .setUploadedAt(r.getUploadedAt() != null ? r.getUploadedAt() : "")
        .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

    if (r.getDeletedAt() != null) {
      b.setDeletedAt(StringValue.of(r.getDeletedAt()));
    }
    return b.build();
  }
}
