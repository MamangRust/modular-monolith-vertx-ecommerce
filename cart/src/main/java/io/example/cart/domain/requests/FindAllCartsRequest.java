package io.example.cart.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllCartsRequest {
    private Integer userId;
    private String search;
    private Integer page;
    private Integer pageSize;
}