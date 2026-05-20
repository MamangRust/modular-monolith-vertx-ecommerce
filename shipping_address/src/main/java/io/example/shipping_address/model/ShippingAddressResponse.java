package io.example.shipping_address.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddressResponse {
    private Long id;
    private Integer orderId;
    private String alamat;
    private String provinsi;
    private String negara;
    private String kota;
    private String courier;
    private String shippingMethod;
    private Integer shippingCost;
    private String createdAt;
    private String updatedAt;

    public static ShippingAddressResponse from(ShippingAddress s) {
        if (s == null) return null;
        return ShippingAddressResponse.builder()
                .id(s.getShippingAddressId())
                .orderId(s.getOrderId())
                .alamat(s.getAlamat())
                .provinsi(s.getProvinsi())
                .negara(s.getNegara())
                .kota(s.getKota())
                .courier(s.getCourier())
                .shippingMethod(s.getShippingMethod())
                .shippingCost(s.getShippingCost())
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                .build();
    }
}
