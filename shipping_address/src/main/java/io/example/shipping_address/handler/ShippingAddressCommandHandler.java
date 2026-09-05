package io.example.shipping_address.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.shipping_address.domain.requests.CreateShippingAddressRequest;
import io.example.shipping_address.domain.requests.UpdateShippingAddressRequest;
import io.example.shipping_address.service.ShippingAddressCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShipping;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingAll;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingDelete;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingDeleteAt;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class ShippingAddressCommandHandler
                implements
                pb.shipping_address.VertxShippingCommandServiceGrpcServer.ShippingCommandServiceApi {
        private final ShippingAddressCommandService service;

        @Override
        public Future<ApiResponseShipping> createShipping(
                        pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest req) {
                var reqDto = CreateShippingAddressRequest.builder()
                                .orderId(req.getOrderId())
                                .alamat(req.getAlamat())
                                .provinsi(req.getProvinsi())
                                .negara(req.getNegara())
                                .kota(req.getKota())
                                .courier(req.getCourier())
                                .shippingMethod(req.getShippingMethod())
                                .shippingCost(req.getShippingCost())
                                .build();

                return service.createShippingAddress(reqDto)
                                .map(data -> ApiResponseShipping.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShipping> updateShipping(
                        pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest req) {
                var reqDto = UpdateShippingAddressRequest.builder()
                                .shippingId(req.getShippingId())
                                .orderId(req.getOrderId())
                                .alamat(req.getAlamat())
                                .provinsi(req.getProvinsi())
                                .negara(req.getNegara())
                                .kota(req.getKota())
                                .courier(req.getCourier())
                                .shippingMethod(req.getShippingMethod())
                                .shippingCost(req.getShippingCost())
                                .build();

                return service.updateShippingAddress(reqDto)
                                .map(data -> ApiResponseShipping.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingDeleteAt> trashedShipping(FindByIdShippingRequest req) {
                return service.trashShippingAddress((long) req.getId())
                                .map(data -> ApiResponseShippingDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingDeleteAt> restoreShipping(FindByIdShippingRequest req) {
                return service.restoreShippingAddress((long) req.getId())
                                .map(data -> ApiResponseShippingDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingDelete> deleteShippingPermanent(
                        FindByIdShippingRequest req) {
                return service.deleteShippingAddressPermanently((long) req.getId())
                                .map(v -> ApiResponseShippingDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Shipping address deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingDelete> deleteShippingByOrderPermanent(
                        FindByIdShippingRequest req) {
                return service.deleteShippingAddressByOrderPermanent((long) req.getId())
                                .map(v -> ApiResponseShippingDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Shipping address by order deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingAll> restoreAllShipping(Empty req) {
                return service.restoreAllShippingAddresses()
                                .map(v -> ApiResponseShippingAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All shipping addresses restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseShippingAll> deleteAllShippingPermanent(Empty req) {
                return service.deleteAllPermanentShippingAddresses()
                                .map(v -> ApiResponseShippingAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All shipping addresses permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.shipping_address.VertxShippingCommandServiceGrpcServer.ShippingCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.CreateShipping, this::createShipping);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.UpdateShipping, this::updateShipping);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.TrashedShipping, this::trashedShipping);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.RestoreShipping, this::restoreShipping);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.DeleteShippingPermanent, this::deleteShippingPermanent);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.DeleteShippingByOrderPermanent, this::deleteShippingByOrderPermanent);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.RestoreAllShipping, this::restoreAllShipping);
    GrpcServerBinder.bind(server, pb.shipping_address.VertxShippingCommandServiceGrpcServer.DeleteAllShippingPermanent, this::deleteAllShippingPermanent);
    return this;
  }
}
