package io.example.transaction.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.domain.requests.UpdateTransactionRequest;
import io.example.transaction.service.TransactionCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.ApiResponseTransaction;
import pb.transaction.TransactionCommon.ApiResponseTransactionAll;
import pb.transaction.TransactionCommon.ApiResponseTransactionDelete;
import pb.transaction.TransactionCommon.ApiResponseTransactionDeleteAt;
import pb.transaction.TransactionCommon.FindByIdTransactionRequest;
import pb.transaction.VertxTransactionCommandServiceGrpcServer;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class TransactionCommandHandler
                implements VertxTransactionCommandServiceGrpcServer.TransactionCommandServiceApi {
        private final TransactionCommandService service;

        @Override
        public Future<ApiResponseTransaction> create(pb.transaction.TransactionCommand.CreateTransactionRequest req) {
                CreateTransactionRequest reqDto = CreateTransactionRequest.builder()
                                .orderID((long) req.getOrderId())
                                .merchantID((long) req.getMerchantId())
                                .paymentMethod(req.getPaymentMethod())
                                .amount(req.getAmount())
                                .paymentStatus(req.getPaymentStatus())
                                .cardNumber(req.getCardNumber())
                                .idempotencyKey(req.getIdempotencyKey())
                                .build();

                return service.createTransaction(reqDto)
                                .map(data -> ApiResponseTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
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
                                .map(data -> ApiResponseTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionDeleteAt> trashedTransaction(FindByIdTransactionRequest req) {
                return service.trashTransaction((long) req.getId())
                                .map(data -> ApiResponseTransactionDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(data)
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionDeleteAt> restoreTransaction(FindByIdTransactionRequest req) {
                return service.restoreTransaction((long) req.getId())
                                .map(data -> ApiResponseTransactionDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(data)
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionDelete> deleteTransactionPermanent(FindByIdTransactionRequest req) {
                return service.deleteTransactionPermanently((long) req.getId())
                                .map(v -> ApiResponseTransactionDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Transaction deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionAll> restoreAllTransaction(Empty req) {
                return service.restoreAllTransactions()
                                .map(v -> ApiResponseTransactionAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All transactions restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionDelete> deleteTransactionByOrderPermanent(FindByIdTransactionRequest req) {
                return service.deleteTransactionByOrderIdPermanently((long) req.getId())
                                .map(v -> ApiResponseTransactionDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Transaction deleted permanently by order id")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseTransactionAll> deleteAllTransactionPermanent(Empty req) {
                return service.deleteAllPermanentTransactions()
                                .map(v -> ApiResponseTransactionAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All transactions permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.transaction.VertxTransactionCommandServiceGrpcServer.TransactionCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.Update, this::update);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.TrashedTransaction, this::trashedTransaction);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.RestoreTransaction, this::restoreTransaction);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.DeleteTransactionPermanent, this::deleteTransactionPermanent);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.RestoreAllTransaction, this::restoreAllTransaction);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.DeleteTransactionByOrderPermanent, this::deleteTransactionByOrderPermanent);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionCommandServiceGrpcServer.DeleteAllTransactionPermanent, this::deleteAllTransactionPermanent);
    return this;
  }
}