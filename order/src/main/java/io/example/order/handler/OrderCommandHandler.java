package io.example.order.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.order.service.OrderCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order.OrderCommon.ApiResponseOrder;
import pb.order.OrderCommon.ApiResponseOrderAll;
import pb.order.OrderCommon.ApiResponseOrderDelete;
import pb.order.OrderCommon.ApiResponseOrderDeleteAt;
import pb.order.OrderCommon.FindByIdOrderRequest;
import pb.order.VertxOrderCommandServiceGrpcServer.OrderCommandServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class OrderCommandHandler implements OrderCommandServiceApi {
        private final OrderCommandService service;

        @Override
        public Future<ApiResponseOrder> create(pb.order.OrderCommand.CreateOrderRequest req) {
                var command = io.example.order.domain.requests.CreateOrderRequest.builder()
                                .merchantId((long) req.getMerchantId())
                                .userId(req.getUserId())
                                .items(req.getItemsList().stream()
                                                .map(item -> io.example.order.domain.requests.CreateOrderItemRequest
                                                                .builder()
                                                                .productId((long) item.getProductId())
                                                                .quantity(item.getQuantity())
                                                                .build())
                                                .toList())
                                .shippingAddress(io.example.order.domain.requests.CreateShippingAddressRequest.builder()
                                                .alamat(req.getShipping().getAlamat())
                                                .provinsi(req.getShipping().getProvinsi())
                                                .negara(req.getShipping().getNegara())
                                                .kota(req.getShipping().getKota())
                                                .courier(req.getShipping().getCourier())
                                                .shippingMethod(req.getShipping().getShippingMethod())
                                                .shippingCost(req.getShipping().getShippingCost())
                                                .build())
                                .build();

                return service.createOrder(command)
                                .map(data -> ApiResponseOrder.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toOrderResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrder> update(pb.order.OrderCommand.UpdateOrderRequest req) {
                var command = io.example.order.domain.requests.UpdateOrderRequest.builder()
                                .orderId((long) req.getOrderId())
                                .userId(req.getUserId())
                                .items(req.getItemsList().stream()
                                                .map(item -> io.example.order.domain.requests.UpdateOrderItemRequest
                                                                .builder()
                                                                .orderItemId((long) item.getOrderItemId())
                                                                .productId((long) item.getProductId())
                                                                .quantity(item.getQuantity())
                                                                .build())
                                                .toList())
                                .shippingAddress(req.hasShipping()
                                                ? io.example.order.domain.requests.UpdateShippingAddressRequest.builder()
                                                                .shippingId((long) req.getShipping().getShippingId())
                                                                .orderId((long) req.getOrderId())
                                                                .alamat(req.getShipping().getAlamat())
                                                                .provinsi(req.getShipping().getProvinsi())
                                                                .negara(req.getShipping().getNegara())
                                                                .kota(req.getShipping().getKota())
                                                                .courier(req.getShipping().getCourier())
                                                                .shippingMethod(req.getShipping().getShippingMethod())
                                                                .shippingCost(req.getShipping().getShippingCost())
                                                                .build()
                                                : null)
                                .build();

                return service.updateOrder(command)
                                .map(data -> ApiResponseOrder.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toOrderResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrderDeleteAt> trashedOrder(FindByIdOrderRequest req) {
                return service.trash((long) req.getId())
                                .map(data -> ApiResponseOrderDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toOrderResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrderDeleteAt> restoreOrder(FindByIdOrderRequest req) {
                return service.restore((long) req.getId())
                                .map(data -> ApiResponseOrderDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toOrderResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrderDelete> deleteOrderPermanent(FindByIdOrderRequest req) {
                return service.deletePermanent((long) req.getId())
                                .map(v -> ApiResponseOrderDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Order deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrderAll> restoreAllOrder(Empty req) {
                return service.restoreAll()
                                .map(v -> ApiResponseOrderAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All orders restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseOrderAll> deleteAllOrderPermanent(Empty req) {
                return service.deleteAllPermanent()
                                .map(v -> ApiResponseOrderAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All orders permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.order.VertxOrderCommandServiceGrpcServer.OrderCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.Update, this::update);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.TrashedOrder, this::trashedOrder);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.RestoreOrder, this::restoreOrder);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.DeleteOrderPermanent, this::deleteOrderPermanent);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.RestoreAllOrder, this::restoreAllOrder);
    GrpcServerBinder.bind(server, pb.order.VertxOrderCommandServiceGrpcServer.DeleteAllOrderPermanent, this::deleteAllOrderPermanent);
    return this;
  }
}