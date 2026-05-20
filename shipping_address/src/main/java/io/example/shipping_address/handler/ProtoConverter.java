package io.example.shipping_address.handler;

import com.google.protobuf.StringValue;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;

public class ProtoConverter {

    public static pb.shipping_address.ShippingAddressCommon.ShippingResponse toProtoResponse(ShippingAddressResponse s) {
        if (s == null) {
            return pb.shipping_address.ShippingAddressCommon.ShippingResponse.getDefaultInstance();
        }
        return pb.shipping_address.ShippingAddressCommon.ShippingResponse.newBuilder()
                .setId(s.getId().intValue())
                .setOrderId(s.getOrderId() != null ? s.getOrderId() : 0)
                .setAlamat(s.getAlamat() != null ? s.getAlamat() : "")
                .setProvinsi(s.getProvinsi() != null ? s.getProvinsi() : "")
                .setNegara(s.getNegara() != null ? s.getNegara() : "")
                .setKota(s.getKota() != null ? s.getKota() : "")
                .setShippingMethod(s.getShippingMethod() != null ? s.getShippingMethod() : "")
                .setShippingCost(s.getShippingCost() != null ? s.getShippingCost() : 0)
                .setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt() : "")
                .setUpdatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt() : "")
                .build();
    }

    public static pb.shipping_address.ShippingAddressCommon.ShippingResponseDeleteAt toProtoResponseDeleteAt(ShippingAddressResponseDeleteAt s) {
        if (s == null) {
            return pb.shipping_address.ShippingAddressCommon.ShippingResponseDeleteAt.getDefaultInstance();
        }
        pb.shipping_address.ShippingAddressCommon.ShippingResponseDeleteAt.Builder b = pb.shipping_address.ShippingAddressCommon.ShippingResponseDeleteAt.newBuilder()
                .setId(s.getId().intValue())
                .setOrderId(s.getOrderId() != null ? s.getOrderId() : 0)
                .setAlamat(s.getAlamat() != null ? s.getAlamat() : "")
                .setProvinsi(s.getProvinsi() != null ? s.getProvinsi() : "")
                .setNegara(s.getNegara() != null ? s.getNegara() : "")
                .setKota(s.getKota() != null ? s.getKota() : "")
                .setShippingMethod(s.getShippingMethod() != null ? s.getShippingMethod() : "")
                .setShippingCost(s.getShippingCost() != null ? s.getShippingCost() : 0)
                .setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt() : "")
                .setUpdatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt() : "");

        if (s.getDeletedAt() != null && !s.getDeletedAt().isEmpty()) {
            b.setDeletedAt(StringValue.of(s.getDeletedAt()));
        }
        return b.build();
    }
}
