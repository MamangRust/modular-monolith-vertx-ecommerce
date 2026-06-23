package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.shipping_address.ShippingAddressCommon;
import pb.shipping_address.ShippingAddressQuery;
import pb.shipping_address.ShippingAddressCommand;
import pb.shipping_address.VertxShippingQueryServiceGrpcClient;
import pb.shipping_address.VertxShippingCommandServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class ShippingAddressProxyHandler {
        private final VertxShippingQueryServiceGrpcClient queryClient;
        private final VertxShippingCommandServiceGrpcClient commandClient;

        public void findAll(RoutingContext ctx) {
                var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
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
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByOrder(RoutingContext ctx) {
                int orderId = GrpcGatewayUtils.getSafePathInt(ctx, "orderId");
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(orderId).build();

                queryClient.findByOrder(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                JsonObject body = ctx.body().asJsonObject();
                var req = ShippingAddressCommand.CreateShippingAddressRequest.newBuilder()
                                .setOrderId(GrpcGatewayUtils.getJsonInteger(body, "order_id", 0))
                                .setAlamat(GrpcGatewayUtils.getJsonString(body, "alamat", ""))
                                .setProvinsi(GrpcGatewayUtils.getJsonString(body, "provinsi", ""))
                                .setKota(GrpcGatewayUtils.getJsonString(body, "kota", ""))
                                .setCourier(GrpcGatewayUtils.getJsonString(body, "courier", ""))
                                .setShippingMethod(GrpcGatewayUtils.getJsonString(body, "shipping_method", ""))
                                .setShippingCost(GrpcGatewayUtils.getJsonInteger(body, "shipping_cost", 0))
                                .setNegara(GrpcGatewayUtils.getJsonString(body, "negara", ""))
                                .build();

                commandClient.createShipping(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void update(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();
                var req = ShippingAddressCommand.UpdateShippingAddressRequest.newBuilder()
                                .setShippingId(id)
                                .setOrderId(GrpcGatewayUtils.getJsonInteger(body, "order_id", 0))
                                .setAlamat(GrpcGatewayUtils.getJsonString(body, "alamat", ""))
                                .setProvinsi(GrpcGatewayUtils.getJsonString(body, "provinsi", ""))
                                .setKota(GrpcGatewayUtils.getJsonString(body, "kota", ""))
                                .setCourier(GrpcGatewayUtils.getJsonString(body, "courier", ""))
                                .setShippingMethod(GrpcGatewayUtils.getJsonString(body, "shipping_method", ""))
                                .setShippingCost(GrpcGatewayUtils.getJsonInteger(body, "shipping_cost", 0))
                                .setNegara(GrpcGatewayUtils.getJsonString(body, "negara", ""))
                                .build();

                commandClient.updateShipping(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

                commandClient.trashedShipping(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

                commandClient.restoreShipping(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

                commandClient.deleteShippingPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteByOrderPermanent(RoutingContext ctx) {
                int orderId = GrpcGatewayUtils.getSafePathInt(ctx, "orderId");
                var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(orderId).build();

                commandClient.deleteShippingByOrderPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllShipping(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllShippingPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }
}