package io.example.order.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateShippingAddressRequest {
    private Long orderId;
    private String alamat;
    private String provinsi;
    private String kota;
    private String courier;
    private String shippingMethod;
    private Integer shippingCost;
    private String negara;
}
