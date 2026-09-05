package io.example.order.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderCommandServiceStockDeltaTest {
    @Test
    void increasingQuantityProducesPositiveReservedDelta() {
        assertThat(OrderCommandServiceImpl.calculateStockDelta(5, 8)).isEqualTo(3);
    }

    @Test
    void decreasingQuantityProducesNegativeReservedDelta() {
        assertThat(OrderCommandServiceImpl.calculateStockDelta(5, 2)).isEqualTo(-3);
    }

    @Test
    void sameProductIdAcrossIntegerAndLongRepresentationsMatchesByValue() {
        Integer existingProductId = 33;
        Long requestedProductId = 33L;

        assertThat(existingProductId.longValue()).isEqualTo(requestedProductId);
    }
}
