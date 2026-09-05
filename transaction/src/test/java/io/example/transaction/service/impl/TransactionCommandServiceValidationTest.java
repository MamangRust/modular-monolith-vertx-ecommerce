package io.example.transaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.transaction.repository.MerchantQueryRepository;
import io.example.transaction.repository.OrderItemRepository;
import io.example.transaction.repository.OrderQueryRepository;
import io.example.transaction.repository.OutboxRepository;
import io.example.transaction.repository.ShippingAddressQueryRepository;
import io.example.transaction.repository.TransactionCommandRepository;
import io.example.transaction.repository.TransactionQueryRepository;
import io.example.transaction.repository.UserQueryRepository;
import io.example.transaction.repository.WalletCommandRepository;

class TransactionCommandServiceValidationTest {
    private final TransactionCommandServiceImpl service = new TransactionCommandServiceImpl(
            mock(TransactionCommandRepository.class),
            mock(TransactionQueryRepository.class),
            mock(MerchantQueryRepository.class),
            mock(OrderQueryRepository.class),
            mock(OrderItemRepository.class),
            mock(ShippingAddressQueryRepository.class),
            mock(UserQueryRepository.class),
            mock(WalletCommandRepository.class),
            mock(OutboxRepository.class),
            mock(RedisService.class),
            mock(TracingMetrics.class),
            mock(KafkaService.class));

    @Test
    void createRejectsNullRequest() {
        var future = service.createTransaction(null);

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage("Transaction request is required");
    }

    @Test
    void createRejectsMissingOrder() {
        var future = service.createTransaction(io.example.transaction.domain.requests.CreateTransactionRequest.builder()
                .merchantID(2L)
                .paymentMethod("card")
                .cardNumber("4111111111111111")
                .build());

        assertThat(future.failed()).isTrue();
        assertThat(future.cause()).hasMessage(
                "Order, merchant, and payment method are required");
    }
}
