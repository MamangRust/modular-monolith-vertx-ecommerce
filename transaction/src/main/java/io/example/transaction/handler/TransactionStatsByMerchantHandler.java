package io.example.transaction.handler;

import io.example.transaction.service.TransactionStatsByMerchantService;
import io.vertx.core.Future;
import pb.transaction.TransactionCommon.*;
import pb.transaction.TransactionStatsBymerchant.*;
import pb.transaction.VertxTransactionStatsByMerchantServiceGrpcServer;

public class TransactionStatsByMerchantHandler implements VertxTransactionStatsByMerchantServiceGrpcServer.TransactionStatsByMerchantServiceApi {
    private final TransactionStatsByMerchantService service;

    public TransactionStatsByMerchantHandler(TransactionStatsByMerchantService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseTransactionMonthAmountSuccess> getMonthlyAmountSuccessByMerchant(MonthAmountTransactionMerchantRequest req) {
        return service.getMonthlyAmountTransactionSuccessByMerchant(req.getMerchantId(), req.getYear(), req.getMonth())
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
    public Future<ApiResponseTransactionYearAmountSuccess> getYearlyAmountSuccessByMerchant(YearAmountTransactionMerchantRequest req) {
        return service.getYearlyAmountTransactionSuccessByMerchant(req.getMerchantId(), req.getYear())
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
    public Future<ApiResponseTransactionMonthAmountFailed> getMonthlyAmountFailedByMerchant(MonthAmountTransactionMerchantRequest req) {
        return service.getMonthlyAmountTransactionFailedByMerchant(req.getMerchantId(), req.getYear(), req.getMonth())
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
    public Future<ApiResponseTransactionYearAmountFailed> getYearlyAmountFailedByMerchant(YearAmountTransactionMerchantRequest req) {
        return service.getYearlyAmountTransactionFailedByMerchant(req.getMerchantId(), req.getYear())
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
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodByMerchantSuccess(MonthMethodTransactionMerchantRequest req) {
        return service.getMonthlyTransactionMethodsByMerchantSuccess(req.getMerchantId(), req.getYear(), req.getMonth())
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
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodByMerchantSuccess(YearMethodTransactionMerchantRequest req) {
        return service.getYearlyTransactionMethodsByMerchantSuccess(req.getMerchantId(), req.getYear())
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
    public Future<ApiResponseTransactionMonthPaymentMethod> getMonthlyTransactionMethodByMerchantFailed(MonthMethodTransactionMerchantRequest req) {
        return service.getMonthlyTransactionMethodsByMerchantFailed(req.getMerchantId(), req.getYear(), req.getMonth())
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
    public Future<ApiResponseTransactionYearPaymentmethod> getYearlyTransactionMethodByMerchantFailed(YearMethodTransactionMerchantRequest req) {
        return service.getYearlyTransactionMethodsByMerchantFailed(req.getMerchantId(), req.getYear())
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
