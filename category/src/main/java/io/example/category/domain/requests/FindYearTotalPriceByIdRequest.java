package io.example.category.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindYearTotalPriceByIdRequest {
    private Integer year;
    private Long categoryId;
}
