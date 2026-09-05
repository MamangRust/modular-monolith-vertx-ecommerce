package io.example.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.product.repository.CategoryQueryRepository;
import io.example.product.repository.MerchantQueryRepository;
import io.example.product.repository.ProductCommandRepository;
import io.example.product.repository.ProductQueryRepository;

class ProductCommandServiceValidationTest {
    private final ProductCommandServiceImpl service = new ProductCommandServiceImpl(
            mock(ProductCommandRepository.class),
            mock(ProductQueryRepository.class),
            mock(CategoryQueryRepository.class),
            mock(MerchantQueryRepository.class),
            mock(RedisService.class),
            mock(TracingMetrics.class));

    @Test
    void incrementStockRejectsNonPositiveQuantity() {
        var future = service.incrementStock(10, 0);

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).isInstanceOf(io.example.common.exception.grpc.BadRequestException.class);
    }

    @Test
    void incrementStockRejectsInvalidProductId() {
        var future = service.incrementStock(0, 1);

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage("Product ID and a positive quantity are required");
    }
}
