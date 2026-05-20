package io.example.transaction.handler;

import com.google.protobuf.Empty;

import io.example.transaction.model.CreateTransactionRequest;
import io.example.transaction.model.UpdateTransactionRequest;
import io.example.transaction.service.TransactionCommandService;
import io.vertx.core.Future;
import pb.transaction.TransactionCommon.ApiResponseTransaction;
import pb.transaction.TransactionCommon.ApiResponseTransactionAll;
import pb.transaction.TransactionCommon.ApiResponseTransactionDelete;
import pb.transaction.TransactionCommon.ApiResponseTransactionDeleteAt;
import pb.transaction.TransactionCommon.FindByIdTransactionRequest;
import pb.transaction.VertxTransactionCommandServiceGrpcServer;

public class TransactionCommandHandler
        implements VertxTransactionCommandServiceGrpcServer.TransactionCommandServiceApi {
    private final TransactionCommandService service;

    public TransactionCommandHandler(TransactionCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseTransaction> create(pb.transaction.TransactionCommand.CreateTransactionRequest req) {
        CreateTransactionRequest reqDto = CreateTransactionRequest.builder()
                .orderID((long) req.getOrderId())
                .merchantID((long) req.getMerchantId())
                .paymentMethod(req.getPaymentMethod())
                .amount(req.getAmount())
                .paymentStatus(req.getPaymentStatus())
                .build();

        return service.createTransaction(reqDto)
                .map(res -> {
                    ApiResponseTransaction.Builder builder = ApiResponseTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransaction> update(pb.transaction.TransactionCommand.UpdateTransactionRequest req) {
        UpdateTransactionRequest reqDto = UpdateTransactionRequest.builder()
                .transactionID((long) req.getTransactionId())
                .orderID((long) req.getOrderId())
                .merchantID((long) req.getMerchantId())
                .paymentMethod(req.getPaymentMethod())
                .amount(req.getAmount())
                .paymentStatus(req.getPaymentStatus())
                .build();

        return service.updateTransaction(reqDto)
                .map(res -> {
                    ApiResponseTransaction.Builder builder = ApiResponseTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionDeleteAt> trashedTransaction(FindByIdTransactionRequest req) {
        return service.trashTransaction((long) req.getId())
                .map(res -> {
                    ApiResponseTransactionDeleteAt.Builder builder = ApiResponseTransactionDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionDeleteAt> restoreTransaction(FindByIdTransactionRequest req) {
        return service.restoreTransaction((long) req.getId())
                .map(res -> {
                    ApiResponseTransactionDeleteAt.Builder builder = ApiResponseTransactionDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionDelete> deleteTransactionPermanent(FindByIdTransactionRequest req) {
        return service.deleteTransactionPermanently((long) req.getId())
                .map(res -> ApiResponseTransactionDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseTransactionAll> restoreAllTransaction(Empty req) {
        return service.restoreAllTransactions()
                .map(res -> ApiResponseTransactionAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseTransactionDelete> deleteTransactionByOrderPermanent(FindByIdTransactionRequest req) {
        return service.deleteTransactionByOrderIdPermanently((long) req.getId())
                .map(res -> ApiResponseTransactionDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseTransactionAll> deleteAllTransactionPermanent(Empty req) {
        return service.deleteAllPermanentTransactions()
                .map(res -> ApiResponseTransactionAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }
}
