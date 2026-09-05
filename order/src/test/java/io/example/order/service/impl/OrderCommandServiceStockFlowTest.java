package io.example.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.model.Order;
import io.example.order.model.OrderItem;
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

/**
 * Test alur stok target (lihat ORDER_TRANSACTION.md §6.1 & §8):
 * - trash mengembalikan stok item aktif (tanpa me-trash item),
 * - restore menurunkan stok lagi (simetris),
 * - deletePermanent wajib order sudah di-trash,
 * - restoreAll bekerja per order dengan anti double-decrement (race).
 */
class OrderCommandServiceStockFlowTest {

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
        // Default stub agar kompensasi/revert tidak mengembalikan null (Mockito
        // return null untuk method non-primitive yang tidak di-stub → NPE).
        when(productCommandRepo.incrementStock(anyInt(), anyInt())).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(anyInt(), anyInt())).thenReturn(Future.succeededFuture());
    }

    private static Order anOrder(Long id, Integer merchantId) {
        return Order.builder().orderId(id).userId(10).merchantId(merchantId).totalPrice(100).build();
    }

    private static OrderItem anItem(Long itemId, Integer productId, int quantity) {
        return OrderItem.builder().orderItemId(itemId).orderId(1).productId(productId)
                .quantity(quantity).price(100).build();
    }

    // ---------------------------------------------------------------------
    // TRASH — restore stok item aktif
    // ---------------------------------------------------------------------

    @Test
    void trashRestoresStockForActiveItemsAndEvicts() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2), anItem(12L, 44, 3));

        when(orderQueryRepo.getOrderById(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.incrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.incrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.trashOrder(1L)).thenReturn(Future.succeededFuture(order));

        var result = service.trash(1L);

        assertThat(result.succeeded()).isTrue();
        verify(productCommandRepo).incrementStock(33, 2);
        verify(productCommandRepo).incrementStock(44, 3);
        verify(orderCommandRepo).trashOrder(1L);
        // trash order TIDAK me-trash item (item di-trash eksplisit per order_item_id)
        verifyNoInteractions(orderItemCommandRepo);
        verify(redis).delete("order:1");
    }

    @Test
    void trashRevertsStockWhenTrashOrderReturnsNullRace() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2));

        when(orderQueryRepo.getOrderById(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.incrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.trashOrder(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.trash(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(NotFoundException.class);
        // stok yang sudah di-restore harus di-revert (decrement balik)
        verify(productCommandRepo).decrementStock(33, 2);
    }

    @Test
    void trashDoesNotTouchStockWhenOrderMissing() {
        when(orderQueryRepo.getOrderById(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.trash(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(productCommandRepo, orderItemCommandRepo);
    }

    @Test
    void trashRevertsStockWhenTrashOrderFails() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2), anItem(12L, 44, 3));

        when(orderQueryRepo.getOrderById(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.incrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.incrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.trashOrder(1L)).thenReturn(Future.failedFuture(new RuntimeException("DB down")));

        var result = service.trash(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).hasMessage("DB down");
        verify(productCommandRepo).decrementStock(33, 2);
        verify(productCommandRepo).decrementStock(44, 3);
    }

    // ---------------------------------------------------------------------
    // RESTORE — decrement stok item aktif lagi (simetris)
    // ---------------------------------------------------------------------

    @Test
    void restoreDecrementsStockForActiveItemsAgainAndEvicts() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2), anItem(12L, 44, 3));

        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(order));

        var result = service.restore(1L);

        assertThat(result.succeeded()).isTrue();
        verify(productCommandRepo).decrementStock(33, 2);
        verify(productCommandRepo).decrementStock(44, 3);
        verify(orderCommandRepo).restoreOrder(1L);
        // restore order TIDAK me-restore item (item tidak pernah di-trash bersama order)
        verifyNoInteractions(orderItemCommandRepo);
        verify(redis).delete("order:1");
    }

    @Test
    void restoreRejectsOrderNotTrashed() {
        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.restore(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(productCommandRepo);
    }

    @Test
    void restoreCompensatesStockWhenRestoreOrderReturnsNullRace() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2));

        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.restore(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(NotFoundException.class);
        // stok yang sudah turun harus dikompensasi (increment balik)
        verify(productCommandRepo).incrementStock(33, 2);
    }

    @Test
    void restoreCompensatesStockWhenRestoreOrderFails() {
        Order order = anOrder(1L, 20);
        List<OrderItem> items = List.of(anItem(11L, 33, 2), anItem(12L, 44, 3));

        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(items));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.failedFuture(new RuntimeException("DB down")));

        var result = service.restore(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).hasMessage("DB down");
        verify(productCommandRepo).incrementStock(33, 2);
        verify(productCommandRepo).incrementStock(44, 3);
    }

    // ---------------------------------------------------------------------
    // DELETE PERMANENT — wajib trashed dulu
    // ---------------------------------------------------------------------

    @Test
    void deletePermanentRejectsOrderNotTrashed() {
        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.deletePermanent(1L);

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(BadRequestException.class);
        verifyNoInteractions(orderItemCommandRepo, shippingAddressCommandRepo, transactionCommandRepo,
                orderCommandRepo);
    }

    @Test
    void deletePermanentCascadesForTrashedOrderAndEvicts() {
        Order order = anOrder(1L, 20);

        when(orderQueryRepo.findByTrashedId(1L)).thenReturn(Future.succeededFuture(order));
        when(orderItemCommandRepo.deleteOrderItemPermanently(1L)).thenReturn(Future.succeededFuture());
        when(shippingAddressCommandRepo.deleteShippingAddressPermanently(1L)).thenReturn(Future.succeededFuture());
        when(transactionCommandRepo.deleteByOrderIDPermanent(1L)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.deleteOrderPermanently(1L)).thenReturn(Future.succeededFuture(true));

        var result = service.deletePermanent(1L);

        assertThat(result.succeeded()).isTrue();
        verify(orderItemCommandRepo).deleteOrderItemPermanently(1L);
        verify(shippingAddressCommandRepo).deleteShippingAddressPermanently(1L);
        verify(transactionCommandRepo).deleteByOrderIDPermanent(1L);
        verify(orderCommandRepo).deleteOrderPermanently(1L);
        verify(redis).delete("order:1");
    }

    // ---------------------------------------------------------------------
    // RESTORE ALL — per order + anti double-decrement
    // ---------------------------------------------------------------------

    @Test
    void restoreAllRestoresEachOrderWithStockDecrement() {
        Order order1 = anOrder(1L, 20);
        Order order2 = anOrder(2L, 21);
        OrderItem item1 = anItem(11L, 33, 2);
        OrderItem item2 = anItem(12L, 44, 3);

        when(orderCommandRepo.findAllTrashed()).thenReturn(Future.succeededFuture(List.of(order1, order2)));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(List.of(item1)));
        when(orderItemQueryRepo.getOrderItemsByOrder(2)).thenReturn(Future.succeededFuture(List.of(item2)));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(order1));
        when(orderCommandRepo.restoreOrder(2L)).thenReturn(Future.succeededFuture(order2));

        var result = service.restoreAll();

        assertThat(result.succeeded()).isTrue();
        verify(productCommandRepo).decrementStock(33, 2);
        verify(productCommandRepo).decrementStock(44, 3);
        verify(orderCommandRepo).restoreOrder(1L);
        verify(orderCommandRepo).restoreOrder(2L);
        // restoreAll TIDAK bulk-restore item
        verifyNoInteractions(orderItemCommandRepo);
    }

    @Test
    void restoreAllSkipsOrderLostInRaceAndUndoesItsDecrement() {
        Order order1 = anOrder(1L, 20);
        Order order2 = anOrder(2L, 21);
        OrderItem item1 = anItem(11L, 33, 2);
        OrderItem item2 = anItem(12L, 44, 3);

        when(orderCommandRepo.findAllTrashed()).thenReturn(Future.succeededFuture(List.of(order1, order2)));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(List.of(item1)));
        when(orderItemQueryRepo.getOrderItemsByOrder(2)).thenReturn(Future.succeededFuture(List.of(item2)));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(44, 3)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(order1));
        // order2 sudah di-restore request lain — restoreOrder atomik mengembalikan null
        when(orderCommandRepo.restoreOrder(2L)).thenReturn(Future.succeededFuture(null));

        var result = service.restoreAll();

        assertThat(result.succeeded()).isTrue();
        verify(productCommandRepo).decrementStock(33, 2);
        verify(productCommandRepo).decrementStock(44, 3);
        // undo decrement order2 (increment balik) — tidak double-decrement
        verify(productCommandRepo).incrementStock(44, 3);
        verify(orderCommandRepo).restoreOrder(1L);
        verify(orderCommandRepo).restoreOrder(2L);
    }

    @Test
    void restoreAllFailsWhenEveryOrderLostInRace() {
        Order order1 = anOrder(1L, 20);
        OrderItem item1 = anItem(11L, 33, 2);

        when(orderCommandRepo.findAllTrashed()).thenReturn(Future.succeededFuture(List.of(order1)));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(List.of(item1)));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(null));

        var result = service.restoreAll();

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(NotFoundException.class);
        verify(productCommandRepo).incrementStock(33, 2);
    }

    @Test
    void restoreAllKeepsPreviousOrdersWhenLaterOrderDecrementFails() {
        Order order1 = anOrder(1L, 20);
        Order order2 = anOrder(2L, 21);
        OrderItem item1 = anItem(11L, 33, 2);
        OrderItem item2 = anItem(12L, 44, 3);

        when(orderCommandRepo.findAllTrashed()).thenReturn(Future.succeededFuture(List.of(order1, order2)));
        when(orderItemQueryRepo.getOrderItemsByOrder(1)).thenReturn(Future.succeededFuture(List.of(item1)));
        when(orderItemQueryRepo.getOrderItemsByOrder(2)).thenReturn(Future.succeededFuture(List.of(item2)));
        when(productCommandRepo.decrementStock(33, 2)).thenReturn(Future.succeededFuture());
        when(productCommandRepo.decrementStock(44, 3))
                .thenReturn(Future.failedFuture(new RuntimeException("Insufficient stock")));
        when(orderCommandRepo.restoreOrder(1L)).thenReturn(Future.succeededFuture(order1));

        var result = service.restoreAll();

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).hasMessage("Insufficient stock");
        // order1 tetap ter-restore (partial completion)
        verify(orderCommandRepo).restoreOrder(1L);
        // stok order2 yang berhasil didecrement (tidak ada di kasus ini) dikompensasi;
        // decrement yang gagal tidak perlu dikompensasi
        verify(productCommandRepo, never()).incrementStock(44, 3);
    }

    @Test
    void restoreAllNotFoundWhenNoTrashedOrders() {
        when(orderCommandRepo.findAllTrashed()).thenReturn(Future.succeededFuture(List.of()));

        var result = service.restoreAll();

        assertThat(result.failed()).isTrue();
        assertThat(result.cause()).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(productCommandRepo, orderItemQueryRepo);
    }

    // ---------------------------------------------------------------------
    // DELETE ALL PERMANENT — tanpa sentuh stok
    // ---------------------------------------------------------------------

    @Test
    void deleteAllPermanentDoesNotTouchStock() {
        when(orderCommandRepo.deleteAllPermanentOrders()).thenReturn(Future.succeededFuture(2));
        when(orderItemCommandRepo.deleteAllPermanentOrderItems()).thenReturn(Future.succeededFuture());
        when(shippingAddressCommandRepo.deleteAllShippingAddress()).thenReturn(Future.succeededFuture());
        when(transactionCommandRepo.deleteAll()).thenReturn(Future.succeededFuture());

        var result = service.deleteAllPermanent();

        assertThat(result.succeeded()).isTrue();
        verifyNoInteractions(productCommandRepo);
        verify(orderCommandRepo).deleteAllPermanentOrders();
    }
}
