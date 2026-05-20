package io.example.shipping_address.handler;

import com.google.protobuf.Empty;
import io.example.shipping_address.model.CreateShippingAddressRequest;
import io.example.shipping_address.model.UpdateShippingAddressRequest;
import io.example.shipping_address.service.ShippingAddressCommandService;
import io.vertx.core.Future;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShipping;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingAll;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingDelete;
import pb.shipping_address.ShippingAddressCommon.ApiResponseShippingDeleteAt;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;

public class ShippingAddressCommandHandler implements pb.shipping_address.VertxShippingCommandServiceGrpcServer.ShippingCommandServiceApi {
    private final io.example.shipping_address.service.ShippingAddressCommandService service;

    public ShippingAddressCommandHandler(io.example.shipping_address.service.ShippingAddressCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseShipping> createShipping(pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest req) {
        CreateShippingAddressRequest reqDto = CreateShippingAddressRequest.builder()
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
                .map(res -> {
                    ApiResponseShipping.Builder builder = ApiResponseShipping.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShipping> updateShipping(pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest req) {
        UpdateShippingAddressRequest reqDto = UpdateShippingAddressRequest.builder()
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
                .map(res -> {
                    ApiResponseShipping.Builder builder = ApiResponseShipping.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShippingDeleteAt> trashedShipping(FindByIdShippingRequest req) {
        return service.trashShippingAddress(req.getId())
                .map(res -> {
                    ApiResponseShippingDeleteAt.Builder builder = ApiResponseShippingDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShippingDeleteAt> restoreShipping(FindByIdShippingRequest req) {
        return service.restoreShippingAddress(req.getId())
                .map(res -> {
                    ApiResponseShippingDeleteAt.Builder builder = ApiResponseShippingDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseShippingDelete> deleteShippingPermanent(FindByIdShippingRequest req) {
        return service.deleteShippingAddressPermanently(req.getId())
                .map(res -> ApiResponseShippingDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseShippingDelete> deleteShippingByOrderPermanent(FindByIdShippingRequest req) {
        return service.deleteShippingAddressByOrderPermanent(req.getId())
                .map(res -> ApiResponseShippingDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseShippingAll> restoreAllShipping(Empty req) {
        return service.restoreAllShippingAddresses()
                .map(res -> ApiResponseShippingAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseShippingAll> deleteAllShippingPermanent(Empty req) {
        return service.deleteAllPermanentShippingAddresses()
                .map(res -> ApiResponseShippingAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }
}
