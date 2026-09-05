package io.example.transaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.enums.PaymentStatus;
import io.example.transaction.model.OrderItem;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.MerchantQueryRepository;
import io.example.transaction.repository.OrderItemRepository;
import io.example.transaction.repository.OrderQueryRepository;
import io.example.transaction.repository.OutboxRepository;
import io.example.transaction.repository.ShippingAddressQueryRepository;
import io.example.transaction.repository.TransactionCommandRepository;
import io.example.transaction.repository.TransactionQueryRepository;
import io.example.transaction.repository.UserQueryRepository;
import io.example.transaction.repository.WalletCommandRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pb.order.OrderCommon.OrderResponse;
import pb.user.UserCommon.UserResponse;

/**
 * Verifies the outbox email event for transactions is deliverable by the email
 * consumer: it must carry email/subject/body (resolved from order -> user),
 * not the old title/message/button/link shape that was silently skipped.
 */
class TransactionCommandServiceEmailPayloadTest {

    private final TransactionCommandRepository repo = mock(TransactionCommandRepository.class);
    private final TransactionQueryRepository queryRepository = mock(TransactionQueryRepository.class);
    private final MerchantQueryRepository merchantRepository = mock(MerchantQueryRepository.class);
    private final OrderQueryRepository orderRepository = mock(OrderQueryRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final ShippingAddressQueryRepository shippingAddressRepository =
            mock(ShippingAddressQueryRepository.class);
    private final UserQueryRepository userQueryRepository = mock(UserQueryRepository.class);
    private final WalletCommandRepository walletRepository = mock(WalletCommandRepository.class);
    private final OutboxRepository outboxRepository = mock(OutboxRepository.class);

    private final TransactionCommandServiceImpl service = new TransactionCommandServiceImpl(
            repo, queryRepository, merchantRepository, orderRepository, orderItemRepository,
            shippingAddressRepository, userQueryRepository, walletRepository, outboxRepository,
            mock(RedisService.class), mock(TracingMetrics.class), mock(KafkaService.class));

    @Test
    void replayPath_writesDeliverableEmailPayloadWithRecipient() {
        Transaction existing = Transaction.builder()
                .transactionId(1L)
                .orderId(1)
                .merchantId(2)
                .amount(50000)
                .status(PaymentStatus.PAID)
                .build();
        when(queryRepository.getTransactionByIdempotencyKey("key-1"))
                .thenReturn(Future.succeededFuture(existing));
        when(orderRepository.getOrderById(1))
                .thenReturn(Future.succeededFuture(OrderResponse.newBuilder().setUserId(7).build()));
        when(userQueryRepository.getUserById(7))
                .thenReturn(Future.succeededFuture(UserResponse.newBuilder()
                        .setEmail("buyer@example.com").build()));
        when(outboxRepository.save(anyString(), anyString(), anyString(),
                any(JsonObject.class), anyString(), anyString()))
                .thenReturn(Future.succeededFuture());

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .orderID(1L)
                .merchantID(2L)
                .paymentMethod("card")
                .cardNumber("4111111111111111")
                .idempotencyKey("key-1")
                .build();

        Future<Transaction> result = service.createTransaction(req);

        assertThat(result.succeeded()).as("Failed: %s", result.cause()).isTrue();
        assertThat(result.result()).isSameAs(existing);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<JsonObject> payloadCaptor = ArgumentCaptor.forClass(JsonObject.class);
        verify(outboxRepository).save(eq("transaction"), eq("1"), eq("transaction.created"),
                payloadCaptor.capture(), eq("email-service-topic-transaction-create"), eq("1"));

        JsonObject emailPayload = payloadCaptor.getValue();
        assertThat(emailPayload.getString("email")).isEqualTo("buyer@example.com");
        assertThat(emailPayload.getString("subject")).isEqualTo("Transaction Successful");
        assertThat(emailPayload.getString("body")).contains("50000");
        // The envelope is baked in at enqueue time, so a replay of the same
        // outbox row keeps the same event_id (idempotency-safe for the email
        // consumer) instead of minting a fresh one at publish.
        assertThat(emailPayload.getString("event_id")).isNotBlank();
        assertThat(emailPayload.getInteger("schema_version")).isEqualTo(1);
        assertThat(emailPayload.getString("event_type")).isEqualTo("transaction.create");
        assertThat(emailPayload.getString("occurred_at")).isNotBlank();
    }

    @Test
    void createPath_writesEnvelopedEmailPayloadToOutboxAtomically() {
        OrderItem item = OrderItem.builder().quantity(1).price(50000).build();
        when(merchantRepository.findById(2)).thenReturn(Future.succeededFuture(true));
        when(orderRepository.findById(1)).thenReturn(Future.succeededFuture(true));
        when(orderItemRepository.findOrderItemByOrder(1))
                .thenReturn(Future.succeededFuture(java.util.List.of(item)));
        when(shippingAddressRepository.findByOrderId(1)).thenReturn(Future.succeededFuture(null));
        when(walletRepository.debit(anyString(), any(Integer.class)))
                .thenReturn(Future.succeededFuture());
        when(orderRepository.getOrderById(1))
                .thenReturn(Future.succeededFuture(OrderResponse.newBuilder().setUserId(7).build()));
        when(userQueryRepository.getUserById(7))
                .thenReturn(Future.succeededFuture(UserResponse.newBuilder()
                        .setEmail("buyer@example.com").build()));

        Transaction created = Transaction.builder().transactionId(1L).build();
        when(repo.createTransactionWithOutbox(any(), any(JsonObject.class), anyString(), anyString(),
                any(JsonObject.class), anyString(), anyString()))
                .thenReturn(Future.succeededFuture(created));

        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .orderID(1L)
                .merchantID(2L)
                .paymentMethod("card")
                .cardNumber("4111111111111111")
                .build();

        Future<Transaction> result = service.createTransaction(req);

        assertThat(result.succeeded()).as("Failed: %s", result.cause()).isTrue();
        assertThat(result.result()).isSameAs(created);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<JsonObject> payloadCaptor = ArgumentCaptor.forClass(JsonObject.class);
        verify(repo).createTransactionWithOutbox(any(), payloadCaptor.capture(),
                eq("email-service-topic-transaction-create"), anyString(),
                any(JsonObject.class), anyString(), anyString());

        JsonObject emailPayload = payloadCaptor.getValue();
        assertThat(emailPayload.getString("email")).isEqualTo("buyer@example.com");
        assertThat(emailPayload.getString("subject")).isEqualTo("Transaction Successful");
        assertThat(emailPayload.getString("body")).contains("55500");
        assertThat(emailPayload.getString("event_id")).isNotBlank();
        assertThat(emailPayload.getInteger("schema_version")).isEqualTo(1);
        assertThat(emailPayload.getString("event_type")).isEqualTo("transaction.create");
        assertThat(emailPayload.getString("occurred_at")).isNotBlank();
    }
}
