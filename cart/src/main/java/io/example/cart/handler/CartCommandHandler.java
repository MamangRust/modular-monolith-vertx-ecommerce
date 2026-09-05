package io.example.cart.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.cart.service.CartCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.cart.CartCommon.ApiResponseCart;
import pb.cart.CartCommon.ApiResponseCartDelete;
import pb.cart.CartCommon.ApiResponseCartAll;
import pb.cart.CartCommand.CreateCartRequest;
import pb.cart.CartCommand.DeleteCartRequest;
import pb.cart.CartCommand.DeleteAllCartRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class CartCommandHandler implements pb.cart.VertxCartCommandServiceGrpcServer.CartCommandServiceApi {
        private final CartCommandService service;

        @Override
        public Future<ApiResponseCart> create(CreateCartRequest req) {
                var reqDomain = io.example.cart.domain.requests.CreateCartRequest.builder()
                                .quantity(req.getQuantity())
                                .productId((long) req.getProductId())
                                .userId(req.getUserId())
                                .build();

                return service.create(reqDomain)
                                .map(data -> ApiResponseCart.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProto(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseCartDelete> delete(DeleteCartRequest req) {
                var reqDomain = io.example.cart.domain.requests.DeleteCartRequest.builder()
                                .cartIds(java.util.List.of((long) req.getCartId()))
                                .userId(req.getUserId())
                                .build();

                return service.deletePermanent(reqDomain)
                                .map(v -> ApiResponseCartDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Cart deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseCartAll> deleteAll(DeleteAllCartRequest req) {
                var reqDomain = io.example.cart.domain.requests.DeleteCartRequest.builder()
                                .cartIds(req.getCartIdsList().stream().map(Integer::longValue).toList())
                                .userId(req.getUserId())
                                .build();

                return service.deleteAll(reqDomain)
                                .map(v -> ApiResponseCartAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All carts deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.cart.VertxCartCommandServiceGrpcServer.CartCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.cart.VertxCartCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.cart.VertxCartCommandServiceGrpcServer.Delete, this::delete);
    GrpcServerBinder.bind(server, pb.cart.VertxCartCommandServiceGrpcServer.DeleteAll, this::deleteAll);
    return this;
  }
}