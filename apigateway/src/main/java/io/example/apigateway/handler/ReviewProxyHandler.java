package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.review.ReviewCommon;
import pb.review.ReviewQuery;
import pb.review.ReviewCommand;
import pb.review.VertxReviewQueryServiceGrpcClient;
import pb.review.VertxReviewCommandServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class ReviewProxyHandler {
        private final VertxReviewQueryServiceGrpcClient queryClient;
        private final VertxReviewCommandServiceGrpcClient commandClient;

        public void findAll(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
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
                var req = ReviewCommon.FindByIdReviewRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByProduct(RoutingContext ctx) {
                int productId = GrpcGatewayUtils.getSafePathInt(ctx, "productId");
                var req = ReviewQuery.FindAllReviewProductRequest.newBuilder()
                                .setProductId(productId)
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByProduct(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");
                var req = ReviewQuery.FindAllReviewMerchantRequest.newBuilder()
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
                var req = ReviewCommand.CreateReviewRequest.newBuilder()
                                .setUserId(GrpcGatewayUtils.getJsonInteger(body, "user_id", 0))
                                .setProductId(GrpcGatewayUtils.getJsonInteger(body, "product_id", 0))
                                .setName(GrpcGatewayUtils.getJsonString(body, "name", ""))
                                .setComment(GrpcGatewayUtils.getJsonString(body, "comment", ""))
                                .setRating(GrpcGatewayUtils.getJsonInteger(body, "rating", 0))
                                .build();

                commandClient.create(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void update(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();
                var req = ReviewCommand.UpdateReviewRequest.newBuilder()
                                .setReviewId(id)
                                .setName(GrpcGatewayUtils.getJsonString(body, "name", ""))
                                .setComment(GrpcGatewayUtils.getJsonString(body, "comment", ""))
                                .setRating(GrpcGatewayUtils.getJsonInteger(body, "rating", 0))
                                .build();

                commandClient.update(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewCommon.FindByIdReviewRequest.newBuilder().setId(id).build();

                commandClient.trashedReview(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewCommon.FindByIdReviewRequest.newBuilder().setId(id).build();

                commandClient.restoreReview(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewCommon.FindByIdReviewRequest.newBuilder().setId(id).build();

                commandClient.deleteReviewPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllReview(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllReviewPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }
}