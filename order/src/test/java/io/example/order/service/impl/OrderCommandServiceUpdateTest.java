package io.example.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.domain.requests.UpdateOrderItemRequest;
import io.example.order.domain.requests.UpdateOrderRequest;
import io.example.order.domain.requests.UpdateShippingAddressRequest;
import io.example.order.model.Order;
import io.example.order.model.OrderItem;
import io.example.order.model.ProductInfo;
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
import io.vertx.core.Future;

class OrderCommandServiceUpdateTest {
    private final OrderCommandRepository orderCommandRepo = mock(OrderCommandRepository.class);
    private final OrderQueryRepository orderQueryRepo = mock(OrderQueryRepository.class);
    private final OrderItemCommandRepository orderItemCommandRepo = mock(OrderItemCommandRepository.class);
    private final OrderItemQueryRepository orderItemQueryRepo = mock(OrderItemQueryRepository.class);
    private final ShippingAddressCommandRepository shippingAddressCommandRepo = mock(ShippingAddressCommandRepository.class);
    private final TransactionCommandRepository transactionCommandRepo = mock(TransactionCommandRepository.class);
    private final UserQueryRepository userQueryRepo = mock(UserQueryRepository.class);
    private final ProductQueryRepository productQueryRepo = mock(ProductQueryRepository.class);
    private final MerchantQueryRepository merchantQueryRepo = mock(MerchantQueryRepository.class);
    private final ProductCommandRepository productCommandRepo = mock(ProductCommandRepository.class);
    private final RedisService redis = mock(RedisService.class);
    private final TracingMetrics metrics = mock(TracingMetrics.class);

    private final OrderCommandServiceImpl service = new OrderCommandServiceImpl(
            orderCommandRepo, orderQueryRepo, orderItemCommandRepo, orderItemQueryRepo,
            shippingAddressCommandRepo, transactionCommandRepo, userQueryRepo, productQueryRepo,
            merchantQueryRepo, productCommandRepo, redis, metrics);

    @BeforeEach
    void setUp() {
        when(redis.delete(any())).thenReturn(Future.succeededFuture(0L));
        when(redis.deleteByPattern(any())).thenReturn(Future.succeededFuture(0L));
    }

    @Test
    void updatesSameProductUsingQuantityDeltaAndDecrementsOnlyTheIncrease() {
        Order existingOrder = Order.builder()
                .orderId(7L).userId(42).merchantId(9).totalPrice(500).build();
        OrderItem existingItem = OrderItem.builder()
                .orderItemId(11L).orderId(7).productId(33).quantity(5).price(100).build();
        ProductInfo product = ProductInfo.builder().productId(33L).price(120).build();
        UpdateOrderRequest request = updateRequest(42, 8);

        when(orderQueryRepo.getOrderById(7L)).thenReturn(Future.succeededFuture(existingOrder));
        when(userQueryRepo.findById(42)).thenReturn(Future.succeededFuture(true));
        when(orderItemQueryRepo.getOrderItemsByOrder(7)).thenReturn(Future.succeededFuture(List.of(existingItem)));
        when(productCommandRepo.decrementStock(33, 3)).thenReturn(Future.succeededFuture());
        when(productQueryRepo.findById(33)).thenReturn(Future.succeededFuture(product));
        when(orderItemCommandRepo.updateOrderItem(any()))
                .thenReturn(Future.succeededFuture(existingItem));
        when(shippingAddressCommandRepo.updateShippingAddress(any()))
                .thenReturn(Future.succeededFuture(null));
        when(orderItemQueryRepo.calculateTotalPrice(7)).thenReturn(Future.succeededFuture(960));
        when(orderCommandRepo.updateOrder(any())).thenReturn(Future.succeededFuture(existingOrder));

        var result = service.updateOrder(request);

        assertThat(result.succeeded()).isTrue();
        verify(productCommandRepo).decrementStock(33, 3);
        verify(orderItemCommandRepo).updateOrderItem(any());
        verify(productCommandRepo, org.mockito.Mockito.never()).incrementStock(anyInt(), anyInt());
    }

    @Test
    void rejectsUpdateWhenAuthenticatedUserDoesNotOwnOrder() {
        Order existingOrder = Order.builder()
                .orderId(7L).userId(99).merchantId(9).build();
        when(orderQueryRepo.getOrderById(7L)).thenReturn(Future.succeededFuture(existingOrder));

        var result = service.updateOrder(updateRequest(42, 8));

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(io.example.common.exception.grpc.ForbiddenException.class);
        verifyNoInteractions(userQueryRepo, orderItemQueryRepo, productCommandRepo,
                productQueryRepo, orderItemCommandRepo, shippingAddressCommandRepo);
    }

    private static UpdateOrderRequest updateRequest(int userId, int quantity) {
        return UpdateOrderRequest.builder()
                .orderId(7L)
                .userId(userId)
                .items(List.of(UpdateOrderItemRequest.builder()
                        .orderItemId(11L).productId(33L).quantity(quantity).price(0).build()))
                .shippingAddress(UpdateShippingAddressRequest.builder()
                        .shippingId(15L).orderId(7L).alamat("Jl. Test")
                        .provinsi("DKI").kota("Jakarta").courier("JNE")
                        .shippingMethod("REG").shippingCost(100).negara("Indonesia")
                        .build())
                .build();
    }
}
