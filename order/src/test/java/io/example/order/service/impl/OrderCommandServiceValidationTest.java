package io.example.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.repository.MerchantQueryRepository;
import io.example.order.repository.OrderCommandRepository;
import io.example.order.repository.OrderItemCommandRepository;
import io.example.order.repository.OrderItemQueryRepository;
import io.example.order.repository.OrderQueryRepository;
import io.example.order.repository.ProductCommandRepository;
import io.example.order.repository.ProductQueryRepository;
import io.example.order.repository.ShippingAddressCommandRepository;
import io.example.order.repository.TransactionCommandRepository;
import io.example.order.repository.UserQueryRepository;

class OrderCommandServiceValidationTest {
    private final OrderCommandServiceImpl service = new OrderCommandServiceImpl(
            mock(OrderCommandRepository.class),
            mock(OrderQueryRepository.class),
            mock(OrderItemCommandRepository.class),
            mock(OrderItemQueryRepository.class),
            mock(ShippingAddressCommandRepository.class),
            mock(TransactionCommandRepository.class),
            mock(UserQueryRepository.class),
            mock(ProductQueryRepository.class),
            mock(MerchantQueryRepository.class),
            mock(ProductCommandRepository.class),
            mock(RedisService.class),
            mock(TracingMetrics.class));

    @Test
    void createRejectsEmptyItems() {
        var future = service.createOrder(io.example.order.domain.requests.CreateOrderRequest.builder()
                .merchantId(1L)
                .userId(2)
                .items(java.util.List.of())
                .shippingAddress(io.example.order.domain.requests.CreateShippingAddressRequest.builder().build())
                .build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).isInstanceOf(io.example.common.exception.grpc.BadRequestException.class);
    }

    @Test
    void createRejectsNonPositiveItemQuantity() {
        var future = service.createOrder(io.example.order.domain.requests.CreateOrderRequest.builder()
                .merchantId(1L)
                .userId(2)
                .items(java.util.List.of(io.example.order.domain.requests.CreateOrderItemRequest.builder()
                        .productId(3L).quantity(0).build()))
                .shippingAddress(io.example.order.domain.requests.CreateShippingAddressRequest.builder().build())
                .build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage("Each order item requires a valid product and positive quantity");
    }
}
