package io.example.transaction.handler;

import com.google.protobuf.StringValue;
import io.example.transaction.model.*;
import pb.transaction.TransactionCommon;

public class ProtoConverter {

    public static TransactionCommon.TransactionResponse toProtoResponse(Transaction tx) {
        if (tx == null) {
            return null;
        }
        return TransactionCommon.TransactionResponse.newBuilder()
                .setId(tx.getTransactionId().intValue())
                .setOrderId(tx.getOrderId() != null ? tx.getOrderId() : 0)
                .setMerchantId(tx.getMerchantId() != null ? tx.getMerchantId() : 0)
                .setPaymentMethod(tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "")
                .setAmount(tx.getAmount() != null ? tx.getAmount() : 0)
                .setPaymentStatus(tx.getStatus() != null ? tx.getStatus().name().toLowerCase() : "")
                .setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "")
                .setUpdatedAt(tx.getUpdatedAt() != null ? tx.getUpdatedAt().toString() : "")
                .build();
    }

    public static TransactionCommon.TransactionResponseDeleteAt toProtoResponseDeleteAt(Transaction tx) {
        if (tx == null) {
            return null;
        }
        TransactionCommon.TransactionResponseDeleteAt.Builder builder = TransactionCommon.TransactionResponseDeleteAt.newBuilder()
                .setId(tx.getTransactionId().intValue())
                .setOrderId(tx.getOrderId() != null ? tx.getOrderId() : 0)
                .setMerchantId(tx.getMerchantId() != null ? tx.getMerchantId() : 0)
                .setPaymentMethod(tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "")
                .setAmount(tx.getAmount() != null ? tx.getAmount() : 0)
                .setPaymentStatus(tx.getStatus() != null ? tx.getStatus().name().toLowerCase() : "")
                .setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "")
                .setUpdatedAt(tx.getUpdatedAt() != null ? tx.getUpdatedAt().toString() : "");

        if (tx.getDeletedAt() != null) {
            builder.setDeletedAt(StringValue.of(tx.getDeletedAt().toString()));
        }

        return builder.build();
    }

    public static TransactionCommon.TransactionMonthlyAmountSuccess toProtoSuccess(
            TransactionMonthlyAmountSuccess item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionMonthlyAmountSuccess.newBuilder()
                .setYear(item.getYear() != null ? item.getYear() : "")
                .setMonth(item.getMonth() != null ? item.getMonth() : "")
                .setTotalSuccess(item.getTotalSuccess() != null ? item.getTotalSuccess() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount() : 0)
                .build();
    }

    public static TransactionCommon.TransactionMonthlyAmountFailed toProtoFailed(TransactionMonthlyAmountFailed item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionMonthlyAmountFailed.newBuilder()
                .setYear(item.getYear() != null ? item.getYear() : "")
                .setMonth(item.getMonth() != null ? item.getMonth() : "")
                .setTotalFailed(item.getTotalFailed() != null ? item.getTotalFailed() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount() : 0)
                .build();
    }

    public static TransactionCommon.TransactionYearlyAmountSuccess toProtoSuccess(TransactionYearlyAmountSuccess item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionYearlyAmountSuccess.newBuilder()
                .setYear(item.getYear() != null ? item.getYear() : "")
                .setTotalSuccess(item.getTotalSuccess() != null ? item.getTotalSuccess() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount() : 0)
                .build();
    }

    public static TransactionCommon.TransactionYearlyAmountFailed toProtoFailed(TransactionYearlyAmountFailed item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionYearlyAmountFailed.newBuilder()
                .setYear(item.getYear() != null ? item.getYear() : "")
                .setTotalFailed(item.getTotalFailed() != null ? item.getTotalFailed() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount() : 0)
                .build();
    }

    public static TransactionCommon.TransactionMonthlyMethod toProtoMethod(TransactionMonthlyMethod item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionMonthlyMethod.newBuilder()
                .setMonth(item.getMonth() != null ? item.getMonth() : "")
                .setPaymentMethod(item.getPaymentMethod() != null ? item.getPaymentMethod() : "")
                .setTotalTransactions(item.getTotalTransactions() != null ? item.getTotalTransactions() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount().intValue() : 0)
                .build();
    }

    public static TransactionCommon.TransactionYearlyMethod toProtoMethod(TransactionYearlyMethod item) {
        if (item == null)
            return null;
        return TransactionCommon.TransactionYearlyMethod.newBuilder()
                .setYear(item.getYear() != null ? item.getYear() : "")
                .setPaymentMethod(item.getPaymentMethod() != null ? item.getPaymentMethod() : "")
                .setTotalTransactions(item.getTotalTransactions() != null ? item.getTotalTransactions() : 0)
                .setTotalAmount(item.getTotalAmount() != null ? item.getTotalAmount().intValue() : 0)
                .build();
    }
}
