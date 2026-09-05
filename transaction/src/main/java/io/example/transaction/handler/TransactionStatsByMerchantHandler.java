package io.example.transaction.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.transaction.domain.requests.FindMonthlyMerchantStatsRequest;
import io.example.transaction.domain.requests.FindYearlyMerchantStatsRequest;
import io.example.transaction.service.TransactionStatsByMerchantService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthAmountFailed;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthAmountSuccess;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthPaymentMethod;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearAmountFailed;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearAmountSuccess;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearPaymentmethod;
import pb.transaction.TransactionStatsBymerchant.MonthAmountTransactionMerchantRequest;
import pb.transaction.TransactionStatsBymerchant.MonthMethodTransactionMerchantRequest;
import pb.transaction.TransactionStatsBymerchant.YearAmountTransactionMerchantRequest;
import pb.transaction.TransactionStatsBymerchant.YearMethodTransactionMerchantRequest;
import pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class TransactionStatsByMerchantHandler
        implements VertxTransactionStatsByMerchantServiceGrpcServer.TransactionStatsByMerchantServiceApi {
    private final TransactionStatsByMerchantService service;

    @Override
    public Future<ApiResponseTransactionMonthAmountSuccess> getMonthlyAmountSuccessByMerchant(
            MonthAmountTransactionMerchantRequest req) {
        return service
                .getMonthlyAmountTransactionSuccessByMerchant(
                        new FindMonthlyMerchantStatsRequest(req.getMerchantId(), req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthAmountSuccess.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoSuccess).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearAmountSuccess> getYearlyAmountSuccessByMerchant(
            YearAmountTransactionMerchantRequest req) {
        return service
                .getYearlyAmountTransactionSuccessByMerchant(
                        new FindYearlyMerchantStatsRequest(req.getMerchantId(), req.getYear()))
                .map(res -> ApiResponseTransactionYearAmountSuccess.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoSuccess).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthAmountFailed> getMonthlyAmountFailedByMerchant(
            MonthAmountTransactionMerchantRequest req) {
        return service
                .getMonthlyAmountTransactionFailedByMerchant(
                        new FindMonthlyMerchantStatsRequest(req.getMerchantId(), req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthAmountFailed.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoFailed).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearAmountFailed> getYearlyAmountFailedByMerchant(
            YearAmountTransactionMerchantRequest req) {
        return service
                .getYearlyAmountTransactionFailedByMerchant(
                        new FindYearlyMerchantStatsRequest(req.getMerchantId(), req.getYear()))
                .map(res -> ApiResponseTransactionYearAmountFailed.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoFailed).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodByMerchantSuccess(
            MonthMethodTransactionMerchantRequest req) {
        return service
                .getMonthlyTransactionMethodsByMerchantSuccess(
                        new FindMonthlyMerchantStatsRequest(req.getMerchantId(), req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthPaymentMethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodByMerchantSuccess(
            YearMethodTransactionMerchantRequest req) {
        return service
                .getYearlyTransactionMethodsByMerchantSuccess(
                        new FindYearlyMerchantStatsRequest(req.getMerchantId(), req.getYear()))
                .map(res -> ApiResponseTransactionYearPaymentmethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodByMerchantFailed(
            MonthMethodTransactionMerchantRequest req) {
        return service
                .getMonthlyTransactionMethodsByMerchantFailed(
                        new FindMonthlyMerchantStatsRequest(req.getMerchantId(), req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthPaymentMethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodByMerchantFailed(
            YearMethodTransactionMerchantRequest req) {
        return service
                .getYearlyTransactionMethodsByMerchantFailed(
                        new FindYearlyMerchantStatsRequest(req.getMerchantId(), req.getYear()))
                .map(res -> ApiResponseTransactionYearPaymentmethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

  @Override
  public pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.TransactionStatsByMerchantServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetMonthlyAmountSuccessByMerchant, this::getMonthlyAmountSuccessByMerchant);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetYearlyAmountSuccessByMerchant, this::getYearlyAmountSuccessByMerchant);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetMonthlyAmountFailedByMerchant, this::getMonthlyAmountFailedByMerchant);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetYearlyAmountFailedByMerchant, this::getYearlyAmountFailedByMerchant);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetMonthlyTransactionMethodByMerchantSuccess, this::getMonthlyTransactionMethodByMerchantSuccess);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetYearlyTransactionMethodByMerchantSuccess, this::getYearlyTransactionMethodByMerchantSuccess);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetMonthlyTransactionMethodByMerchantFailed, this::getMonthlyTransactionMethodByMerchantFailed);
    GrpcServerBinder.bind(server, pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer.GetYearlyTransactionMethodByMerchantFailed, this::getYearlyTransactionMethodByMerchantFailed);
    return this;
  }
}