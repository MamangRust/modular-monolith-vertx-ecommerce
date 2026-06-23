package io.example.transaction.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.transaction.domain.requests.FindMonthlyStatsRequest;
import io.example.transaction.service.TransactionStatsService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthAmountFailed;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthAmountSuccess;
import pb.transaction.TransactionCommon.ApiResponseTransactionMonthPaymentMethod;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearAmountFailed;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearAmountSuccess;
import pb.transaction.TransactionCommon.ApiResponseTransactionYearPaymentmethod;
import pb.transaction.TransactionStats.MonthAmountTransactionRequest;
import pb.transaction.TransactionStats.MonthMethodTransactionRequest;
import pb.transaction.TransactionStats.YearAmountTransactionRequest;
import pb.transaction.TransactionStats.YearMethodTransactionRequest;
import pb.transaction.VertxTransactionStatsServiceGrpcServer;

@RequiredArgsConstructor
public class TransactionStatsHandler implements VertxTransactionStatsServiceGrpcServer.TransactionStatsServiceApi {
    private final TransactionStatsService service;

    @Override
    public Future<ApiResponseTransactionMonthAmountSuccess> getMonthlyAmountSuccess(MonthAmountTransactionRequest req) {
        return service.getMonthlyAmountTransactionSuccess(new FindMonthlyStatsRequest(req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthAmountSuccess.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoSuccess).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearAmountSuccess> getYearlyAmountSuccess(YearAmountTransactionRequest req) {
        return service.getYearlyAmountTransactionSuccess(req.getYear())
                .map(res -> ApiResponseTransactionYearAmountSuccess.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoSuccess).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthAmountFailed> getMonthlyAmountFailed(MonthAmountTransactionRequest req) {
        return service.getMonthlyAmountTransactionFailed(new FindMonthlyStatsRequest(req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthAmountFailed.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoFailed).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearAmountFailed> getYearlyAmountFailed(YearAmountTransactionRequest req) {
        return service.getYearlyAmountTransactionFailed(req.getYear())
                .map(res -> ApiResponseTransactionYearAmountFailed.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoFailed).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodSuccess(
            MonthMethodTransactionRequest req) {
        return service.getMonthlyTransactionMethodsSuccess(new FindMonthlyStatsRequest(req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthPaymentMethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodSuccess(
            YearMethodTransactionRequest req) {
        return service.getYearlyTransactionMethodsSuccess(req.getYear())
                .map(res -> ApiResponseTransactionYearPaymentmethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodFailed(
            MonthMethodTransactionRequest req) {
        return service.getMonthlyTransactionMethodsFailed(new FindMonthlyStatsRequest(req.getYear(), req.getMonth()))
                .map(res -> ApiResponseTransactionMonthPaymentMethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodFailed(
            YearMethodTransactionRequest req) {
        return service.getYearlyTransactionMethodsFailed(req.getYear())
                .map(res -> ApiResponseTransactionYearPaymentmethod.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toProtoMethod).toList())
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }
}