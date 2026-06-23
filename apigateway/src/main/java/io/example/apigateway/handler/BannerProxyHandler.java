package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.BadRequestException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import pb.banner.BannerCommon;
import pb.banner.BannerQuery;
import pb.banner.BannerCommand;
import pb.banner.VertxBannerQueryServiceGrpcClient;
import pb.banner.VertxBannerCommandServiceGrpcClient;

@RequiredArgsConstructor
public class BannerProxyHandler {
        private final VertxBannerQueryServiceGrpcClient queryClient;
        private final VertxBannerCommandServiceGrpcClient commandClient;

        public void findAll(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                JsonObject body = ctx.body().asJsonObject();
                String name = GrpcGatewayUtils.getJsonString(body, "name", "");

                if (name.isBlank()) {
                        ctx.fail(new BadRequestException("Banner 'name' is required"));
                        return;
                }

                var req = BannerCommand.CreateBannerRequest.newBuilder()
                                .setName(name)
                                .setStartDate(GrpcGatewayUtils.getJsonString(body, "start_date", ""))
                                .setEndDate(GrpcGatewayUtils.getJsonString(body, "end_date", ""))
                                .setStartTime(GrpcGatewayUtils.getJsonString(body, "start_time", ""))
                                .setEndTime(GrpcGatewayUtils.getJsonString(body, "end_time", ""))
                                .setIsActive(body.getBoolean("is_active", false))
                                .build();

                commandClient.create(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void update(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();
                String name = GrpcGatewayUtils.getJsonString(body, "name", "");

                if (name.isBlank()) {
                        ctx.fail(new BadRequestException("Banner 'name' is required"));
                        return;
                }

                var req = BannerCommand.UpdateBannerRequest.newBuilder()
                                .setBannerId(id)
                                .setName(name)
                                .setStartDate(GrpcGatewayUtils.getJsonString(body, "start_date", ""))
                                .setEndDate(GrpcGatewayUtils.getJsonString(body, "end_date", ""))
                                .setStartTime(GrpcGatewayUtils.getJsonString(body, "start_time", ""))
                                .setEndTime(GrpcGatewayUtils.getJsonString(body, "end_time", ""))
                                .setIsActive(body.getBoolean("is_active", false))
                                .build();

                commandClient.update(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

                commandClient.trash(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

                commandClient.restore(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

                commandClient.deletePermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAll(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAll(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        private BannerQuery.FindAllBannerRequest buildPaginationRequest(RoutingContext ctx) {
                int page = GrpcGatewayUtils.getQueryInt(ctx, "page", 1);
                int pageSize = GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10);
                String search = GrpcGatewayUtils.getQueryString(ctx, "search", "");

                return BannerQuery.FindAllBannerRequest.newBuilder()
                                .setSearch(search)
                                .setPage(page)
                                .setPageSize(pageSize)
                                .build();
        }
}