package io.example.cart.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.example.cart.repository.CartCommandRepository;
import io.example.cart.repository.ProductQueryRepository;
import io.example.cart.repository.UserQueryRepository;
import io.example.cart.domain.requests.CreateCartRequest;
import io.example.cart.domain.requests.DeleteCartRequest;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;

class CartCommandServiceValidationTest {
    private final CartCommandServiceImpl service = new CartCommandServiceImpl(
            mock(CartCommandRepository.class),
            mock(ProductQueryRepository.class),
            mock(UserQueryRepository.class),
            mock(RedisService.class),
            mock(TracingMetrics.class));

    @Test
    void createRejectsNonPositiveQuantity() {
        var future = service.create(CreateCartRequest.builder()
                .userId(1)
                .productId(2L)
                .quantity(0)
                .build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage("User ID, product ID, and a positive quantity are required");
    }

    @Test
    void createRejectsMissingUserAndProduct() {
        var future = service.create(CreateCartRequest.builder().quantity(1).build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).isInstanceOf(io.example.common.exception.grpc.BadRequestException.class);
    }

    @Test
    void deleteRejectsNullRequest() {
        var future = service.deletePermanent(null);

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).isInstanceOf(io.example.common.exception.grpc.BadRequestException.class);
    }

    @Test
    void deleteAllRejectsEmptyIds() {
        var future = service.deleteAll(DeleteCartRequest.builder().userId(1).build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage("User ID and Cart IDs are required");
    }
}
