package io.example.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    @Test
    void shouldAllowRequestsWithinLimit() {
        var bucket = new RateLimiter.Bucket(5, 60_000);
        for (int i = 0; i < 5; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
    }

    @Test
    void shouldBlockRequestsAfterLimit() {
        var bucket = new RateLimiter.Bucket(3, 60_000);
        for (int i = 0; i < 3; i++) {
            assertThat(bucket.tryConsume()).isTrue();
        }
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void shouldRefillAfterTimeWindow() throws InterruptedException {
        // Use a very short window (100ms) with 1 token
        var bucket = new RateLimiter.Bucket(1, 100);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();

        // Wait for refill window
        Thread.sleep(150);
        assertThat(bucket.tryConsume()).isTrue();
    }

    @Test
    void highConcurrencyShouldNotExceedLimit() throws InterruptedException {
        int limit = 20;
        int windowMs = 2000;
        var bucket = new RateLimiter.Bucket(limit, windowMs);

        // Use a latch for precise coordination
        int threadCount = 5;
        var latch = new java.util.concurrent.CountDownLatch(1);
        var allowed = new java.util.concurrent.atomic.AtomicInteger(0);

        var threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> {
                try {
                    latch.await(); // all threads start at the same time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int i = 0; i < 10; i++) {
                    if (bucket.tryConsume()) {
                        allowed.incrementAndGet();
                    }
                }
            });
            threads[t].start();
        }

        latch.countDown(); // fire all threads simultaneously

        for (var t : threads) {
            t.join();
        }

        assertThat(allowed.get()).isLessThanOrEqualTo(limit);
    }
}
