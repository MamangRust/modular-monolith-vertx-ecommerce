package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.transaction.TransactionCommon;
import pb.transaction.TransactionQuery;
import pb.transaction.TransactionCommand;
import pb.transaction.VertxTransactionQueryServiceGrpcClient;
import pb.transaction.VertxTransactionCommandServiceGrpcClient;
import pb.transaction.VertxTransactionStatsServiceGrpcClient;
import pb.transaction.VertxTransactionStatsByMerchantServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class TransactionProxyHandler {
        private final VertxTransactionQueryServiceGrpcClient queryClient;
        private final VertxTransactionCommandServiceGrpcClient commandClient;
        private final VertxTransactionStatsServiceGrpcClient statsClient;
        private final VertxTransactionStatsByMerchantServiceGrpcClient statsByMerchantClient;


        public void findAll(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAllTransactions(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByOrderId(RoutingContext ctx) {
                int orderId = GrpcGatewayUtils.getSafePathInt(ctx, "orderId");
                var req = TransactionQuery.FindByOrderIdTransactionRequest.newBuilder().setOrderId(orderId).build();

                queryClient.findByOrderId(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = TransactionQuery.FindAllTransactionByMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }


        public void create(RoutingContext ctx) {
                JsonObject body = ctx.body().asJsonObject();
                var req = TransactionCommand.CreateTransactionRequest.newBuilder()
                                .setOrderId(GrpcGatewayUtils.getJsonInteger(body, "order_id", 0))
                                .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
                                .setPaymentMethod(GrpcGatewayUtils.getJsonString(body, "payment_method", ""))
                                .setAmount(GrpcGatewayUtils.getJsonInteger(body, "amount", 0))
                                .setUserId(GrpcGatewayUtils.getJsonInteger(body, "user_id", 0))
                                .setPaymentStatus(GrpcGatewayUtils.getJsonString(body, "payment_status", ""))
                                .build();

                commandClient.create(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void update(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();
                var req = TransactionCommand.UpdateTransactionRequest.newBuilder()
                                .setTransactionId(id)
                                .setOrderId(GrpcGatewayUtils.getJsonInteger(body, "order_id", 0))
                                .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
                                .setPaymentMethod(GrpcGatewayUtils.getJsonString(body, "payment_method", ""))
                                .setAmount(GrpcGatewayUtils.getJsonInteger(body, "amount", 0))
                                .setPaymentStatus(GrpcGatewayUtils.getJsonString(body, "payment_status", ""))
                                .build();

                commandClient.update(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }


        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.trashedTransaction(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.restoreTransaction(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.deleteTransactionPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteByOrderPermanent(RoutingContext ctx) {
                int orderId = GrpcGatewayUtils.getSafePathInt(ctx, "orderId");
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(orderId).build();

                commandClient.deleteTransactionByOrderPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllTransaction(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllTransactionPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyAmountSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthAmountTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsClient.getMonthlyAmountSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyAmountSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearAmountTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsClient.getYearlyAmountSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyAmountFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthAmountTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsClient.getMonthlyAmountFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyAmountFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearAmountTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsClient.getYearlyAmountFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyTransactionMethodSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthMethodTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsClient.getMonthlyTransactionMethodSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyTransactionMethodSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearMethodTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsClient.getYearlyTransactionMethodSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyTransactionMethodFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthMethodTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsClient.getMonthlyTransactionMethodFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyTransactionMethodFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearMethodTransactionRequest.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsClient.getYearlyTransactionMethodFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyAmountSuccessByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.MonthAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsByMerchantClient.getMonthlyAmountSuccessByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyAmountSuccessByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.YearAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsByMerchantClient.getYearlyAmountSuccessByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyAmountFailedByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.MonthAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsByMerchantClient.getMonthlyAmountFailedByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyAmountFailedByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.YearAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsByMerchantClient.getYearlyAmountFailedByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyTransactionMethodSuccessByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.MonthMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsByMerchantClient.getMonthlyTransactionMethodByMerchantSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyTransactionMethodSuccessByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.YearMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsByMerchantClient.getYearlyTransactionMethodByMerchantSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getMonthlyTransactionMethodFailedByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.MonthMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();
                statsByMerchantClient.getMonthlyTransactionMethodByMerchantFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void getYearlyTransactionMethodFailedByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = pb.transaction.TransactionStatsBymerchant.YearMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();
                statsByMerchantClient.getYearlyTransactionMethodByMerchantFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }
}