package io.example.shipping_address.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllShippingAddress {
    private int page;
    private int pageSize;
    private String search;
}
