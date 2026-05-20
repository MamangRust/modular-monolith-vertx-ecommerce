package io.example.cart.handler;

import io.example.cart.service.CartCommandService;
import io.vertx.core.Future;
import pb.cart.CartCommon.ApiResponseCart;
import pb.cart.CartCommon.ApiResponseCartDelete;
import pb.cart.CartCommon.ApiResponseCartAll;
import pb.cart.CartCommand.CreateCartRequest;
import pb.cart.CartCommand.DeleteCartRequest;
import pb.cart.CartCommand.DeleteAllCartRequest;

public class CartCommandHandler implements pb.cart.VertxCartCommandServiceGrpcServer.CartCommandServiceApi {
    private final CartCommandService service;

    public CartCommandHandler(CartCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseCart> create(CreateCartRequest req) {
        return service.create(req)
                .map(resp -> {
                    var builder = ApiResponseCart.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toProto(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseCartDelete> delete(DeleteCartRequest req) {
        return service.deletePermanent(req)
                .map(resp -> ApiResponseCartDelete.newBuilder()
                        .setStatus(resp.status() != null ? resp.status() : "")
                        .setMessage(resp.message() != null ? resp.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseCartAll> deleteAll(DeleteAllCartRequest req) {
        return service.deleteAll(req)
                .map(resp -> ApiResponseCartAll.newBuilder()
                        .setStatus(resp.status() != null ? resp.status() : "")
                        .setMessage(resp.message() != null ? resp.message() : "")
                        .build());
    }
}
