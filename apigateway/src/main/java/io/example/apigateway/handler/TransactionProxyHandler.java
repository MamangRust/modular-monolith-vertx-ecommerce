package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.transaction.TransactionCommon;
import pb.transaction.TransactionQuery;
import pb.transaction.TransactionCommand;
import pb.transaction.VertxTransactionQueryServiceGrpcClient;
import pb.transaction.VertxTransactionCommandServiceGrpcClient;
import pb.transaction.VertxTransactionStatsServiceGrpcClient;
import pb.transaction.VertxTransactionStatsByMerchantServiceGrpcClient;

public class TransactionProxyHandler {
        private final VertxTransactionQueryServiceGrpcClient queryClient;
        private final VertxTransactionCommandServiceGrpcClient commandClient;
        private final VertxTransactionStatsServiceGrpcClient statsClient;
        private final VertxTransactionStatsByMerchantServiceGrpcClient statsByMerchantClient;

        public TransactionProxyHandler(
                        VertxTransactionQueryServiceGrpcClient queryClient,
                        VertxTransactionCommandServiceGrpcClient commandClient,
                        VertxTransactionStatsServiceGrpcClient statsClient,
                        VertxTransactionStatsByMerchantServiceGrpcClient statsByMerchantClient) {
                this.queryClient = queryClient;
                this.commandClient = commandClient;
                this.statsClient = statsClient;
                this.statsByMerchantClient = statsByMerchantClient;
        }

        public void findAll(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search")
                                                : "")
                                .setPage(ctx.queryParams().contains("page")
                                                ? Integer.parseInt(ctx.queryParams().get("page"))
                                                : 1)
                                .setPageSize(ctx.queryParams().contains("pageSize")
                                                ? Integer.parseInt(ctx.queryParams().get("pageSize"))
                                                : 10)
                                .build();

                queryClient.findAllTransactions(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void findActive(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search")
                                                : "")
                                .setPage(ctx.queryParams().contains("page")
                                                ? Integer.parseInt(ctx.queryParams().get("page"))
                                                : 1)
                                .setPageSize(ctx.queryParams().contains("pageSize")
                                                ? Integer.parseInt(ctx.queryParams().get("pageSize"))
                                                : 10)
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void findTrashed(RoutingContext ctx) {
                var req = TransactionQuery.FindAllTransactionRequest.newBuilder()
                                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search")
                                                : "")
                                .setPage(ctx.queryParams().contains("page")
                                                ? Integer.parseInt(ctx.queryParams().get("page"))
                                                : 1)
                                .setPageSize(ctx.queryParams().contains("pageSize")
                                                ? Integer.parseInt(ctx.queryParams().get("pageSize"))
                                                : 10)
                                .build();

                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void findById(RoutingContext ctx) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void findByOrderId(RoutingContext ctx) {
                int orderId = Integer.parseInt(ctx.pathParam("orderId"));
                var req = TransactionQuery.FindByOrderIdTransactionRequest.newBuilder().setOrderId(orderId).build();

                queryClient.findByOrderId(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void findByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = TransactionQuery.FindAllTransactionByMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search")
                                                : "")
                                .setPage(ctx.queryParams().contains("page")
                                                ? Integer.parseInt(ctx.queryParams().get("page"))
                                                : 1)
                                .setPageSize(ctx.queryParams().contains("pageSize")
                                                ? Integer.parseInt(ctx.queryParams().get("pageSize"))
                                                : 10)
                                .build();

                queryClient.findByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void create(RoutingContext ctx) {
                JsonObject body = ctx.body().asJsonObject();
                var req = TransactionCommand.CreateTransactionRequest.newBuilder()
                                .setOrderId(body.getInteger("order_id", 0))
                                .setMerchantId(body.getInteger("merchant_id", 0))
                                .setPaymentMethod(body.getString("payment_method", ""))
                                .setAmount(body.getInteger("amount", 0))
                                .setUserId(body.getInteger("user_id", 0))
                                .setPaymentStatus(body.getString("payment_status", ""))
                                .build();

                commandClient.create(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(ctx::fail);
        }

        public void update(RoutingContext ctx) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                JsonObject body = ctx.body().asJsonObject();
                var req = TransactionCommand.UpdateTransactionRequest.newBuilder()
                                .setTransactionId(id)
                                .setOrderId(body.getInteger("order_id", 0))
                                .setMerchantId(body.getInteger("merchant_id", 0))
                                .setPaymentMethod(body.getString("payment_method", ""))
                                .setAmount(body.getInteger("amount", 0))
                                .setPaymentStatus(body.getString("payment_status", ""))
                                .build();

                commandClient.update(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void trash(RoutingContext ctx) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.trashedTransaction(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void restore(RoutingContext ctx) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.restoreTransaction(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = Integer.parseInt(ctx.pathParam("id"));
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(id).build();

                commandClient.deleteTransactionPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void deleteByOrderPermanent(RoutingContext ctx) {
                int orderId = Integer.parseInt(ctx.pathParam("orderId"));
                var req = TransactionCommon.FindByIdTransactionRequest.newBuilder().setId(orderId).build();

                commandClient.deleteTransactionByOrderPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllTransaction(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllTransactionPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        // Stats
        public void getMonthlyAmountSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthAmountTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsClient.getMonthlyAmountSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyAmountSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearAmountTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsClient.getYearlyAmountSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyAmountFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthAmountTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsClient.getMonthlyAmountFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyAmountFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearAmountTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsClient.getYearlyAmountFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyTransactionMethodSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthMethodTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsClient.getMonthlyTransactionMethodSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyTransactionMethodSuccess(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearMethodTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsClient.getYearlyTransactionMethodSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyTransactionMethodFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.MonthMethodTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsClient.getMonthlyTransactionMethodFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyTransactionMethodFailed(RoutingContext ctx) {
                var req = pb.transaction.TransactionStats.YearMethodTransactionRequest.newBuilder()
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsClient.getYearlyTransactionMethodFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        // Stats By Merchant
        public void getMonthlyAmountSuccessByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.MonthAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsByMerchantClient.getMonthlyAmountSuccessByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyAmountSuccessByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.YearAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsByMerchantClient.getYearlyAmountSuccessByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyAmountFailedByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.MonthAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsByMerchantClient.getMonthlyAmountFailedByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyAmountFailedByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.YearAmountTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsByMerchantClient.getYearlyAmountFailedByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyTransactionMethodSuccessByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.MonthMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsByMerchantClient.getMonthlyTransactionMethodByMerchantSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyTransactionMethodSuccessByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.YearMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsByMerchantClient.getYearlyTransactionMethodByMerchantSuccess(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getMonthlyTransactionMethodFailedByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.MonthMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .setMonth(ctx.queryParams().contains("month")
                                                ? Integer.parseInt(ctx.queryParams().get("month"))
                                                : 0)
                                .build();
                statsByMerchantClient.getMonthlyTransactionMethodByMerchantFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        public void getYearlyTransactionMethodFailedByMerchant(RoutingContext ctx) {
                int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
                var req = pb.transaction.TransactionStatsBymerchant.YearMethodTransactionMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setYear(ctx.queryParams().contains("year")
                                                ? Integer.parseInt(ctx.queryParams().get("year"))
                                                : 0)
                                .build();
                statsByMerchantClient.getYearlyTransactionMethodByMerchantFailed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(ctx::fail);
        }

        private void sendResponse(RoutingContext ctx, com.google.protobuf.MessageOrBuilder proto, int defaultStatus) {
                JsonObject json = ProtoMapper.toJson(proto);
                int status = json.getInteger("status", defaultStatus);
                ctx.response()
                                .setStatusCode(status == 0 ? defaultStatus : status)
                                .putHeader("Content-Type", "application/json")
                                .end(json.encode());
        }
}