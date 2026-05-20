package io.example.transaction.handler;

import io.example.transaction.service.TransactionStatsService;
import io.vertx.core.Future;
import pb.transaction.TransactionCommon.*;
import pb.transaction.TransactionStats.*;
import pb.transaction.VertxTransactionStatsServiceGrpcServer;

public class TransactionStatsHandler implements VertxTransactionStatsServiceGrpcServer.TransactionStatsServiceApi {
    private final TransactionStatsService service;

    public TransactionStatsHandler(TransactionStatsService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseTransactionMonthAmountSuccess> getMonthlyAmountSuccess(MonthAmountTransactionRequest req) {
        return service.getMonthlyAmountTransactionSuccess(req.getYear(), req.getMonth())
                .map(res -> {
                    ApiResponseTransactionMonthAmountSuccess.Builder builder = ApiResponseTransactionMonthAmountSuccess.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoSuccess)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionYearAmountSuccess> getYearlyAmountSuccess(YearAmountTransactionRequest req) {
        return service.getYearlyAmountTransactionSuccess(req.getYear())
                .map(res -> {
                    ApiResponseTransactionYearAmountSuccess.Builder builder = ApiResponseTransactionYearAmountSuccess.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoSuccess)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionMonthAmountFailed> getMonthlyAmountFailed(MonthAmountTransactionRequest req) {
        return service.getMonthlyAmountTransactionFailed(req.getYear(), req.getMonth())
                .map(res -> {
                    ApiResponseTransactionMonthAmountFailed.Builder builder = ApiResponseTransactionMonthAmountFailed.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoFailed)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionYearAmountFailed> getYearlyAmountFailed(YearAmountTransactionRequest req) {
        return service.getYearlyAmountTransactionFailed(req.getYear())
                .map(res -> {
                    ApiResponseTransactionYearAmountFailed.Builder builder = ApiResponseTransactionYearAmountFailed.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoFailed)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodSuccess(MonthMethodTransactionRequest req) {
        return service.getMonthlyTransactionMethodsSuccess(req.getYear(), req.getMonth())
                .map(res -> {
                    ApiResponseTransactionMonthPaymentMethod.Builder builder = ApiResponseTransactionMonthPaymentMethod.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoMethod)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodSuccess(YearMethodTransactionRequest req) {
        return service.getYearlyTransactionMethodsSuccess(req.getYear())
                .map(res -> {
                    ApiResponseTransactionYearPaymentmethod.Builder builder = ApiResponseTransactionYearPaymentmethod.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoMethod)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodFailed(MonthMethodTransactionRequest req) {
        return service.getMonthlyTransactionMethodsFailed(req.getYear(), req.getMonth())
                .map(res -> {
                    ApiResponseTransactionMonthPaymentMethod.Builder builder = ApiResponseTransactionMonthPaymentMethod.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoMethod)
                                .toList());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodFailed(YearMethodTransactionRequest req) {
        return service.getYearlyTransactionMethodsFailed(req.getYear())
                .map(res -> {
                    ApiResponseTransactionYearPaymentmethod.Builder builder = ApiResponseTransactionYearPaymentmethod.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream()
                                .map(ProtoConverter::toProtoMethod)
                                .toList());
                    }

                    return builder.build();
                });
    }
}
