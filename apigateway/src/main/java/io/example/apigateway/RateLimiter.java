package io.example.apigateway;

/**
 * Simple token-bucket rate limiter for per-IP throttling.
 * Each bucket has a max capacity and refills at a fixed rate.
 */
public class RateLimiter {
    private final int maxTokens;
    private final long windowMs;
    private final long refillIntervalMs;
    private final int tokensPerRefill;

    // Not a real instance — buckets are created per-IP via Bucket inner class
    public RateLimiter(int maxRequests, long windowMs) {
        this.maxTokens = maxRequests;
        this.windowMs = windowMs;
        this.refillIntervalMs = windowMs / maxRequests;
        this.tokensPerRefill = 1;
    }

    public static class Bucket {
        private final int maxTokens;
        private final long refillIntervalMs;
        private final int tokensPerRefill;
        private int tokens;
        private long lastRefill;

        public Bucket(int maxRequests, long windowMs) {
            this.maxTokens = maxRequests;
            this.tokens = maxRequests;
            this.lastRefill = System.currentTimeMillis();
            // Refill 1 token every (windowMs / maxRequests) ms
            this.refillIntervalMs = Math.max(1, windowMs / maxRequests);
            this.tokensPerRefill = 1;
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefill;
            if (elapsed >= refillIntervalMs) {
                int add = (int) (elapsed / refillIntervalMs) * tokensPerRefill;
                tokens = Math.min(maxTokens, tokens + add);
                lastRefill = now;
            }
        }
    }
}
