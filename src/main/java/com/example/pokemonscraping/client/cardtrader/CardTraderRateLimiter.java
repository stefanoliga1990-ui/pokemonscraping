package com.example.pokemonscraping.client.cardtrader;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

final class CardTraderRateLimiter {

    private final long generalIntervalNanos;
    private final long marketplaceIntervalNanos;
    private final LongSupplier nanoTime;
    private final Sleeper sleeper;
    private long nextGeneralRequest;
    private long nextMarketplaceRequest;

    CardTraderRateLimiter(Duration generalInterval, Duration marketplaceInterval) {
        this(generalInterval, marketplaceInterval, System::nanoTime, duration -> Thread.sleep(duration.toMillis()));
    }

    CardTraderRateLimiter(
            Duration generalInterval,
            Duration marketplaceInterval,
            LongSupplier nanoTime,
            Sleeper sleeper
    ) {
        this.generalIntervalNanos = requireNonNegative(generalInterval, "generalInterval").toNanos();
        this.marketplaceIntervalNanos = requireNonNegative(marketplaceInterval, "marketplaceInterval").toNanos();
        this.nanoTime = Objects.requireNonNull(nanoTime);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    synchronized void acquire(boolean marketplace) {
        long now = nanoTime.getAsLong();
        long allowedAt = marketplace ? Math.max(nextGeneralRequest, nextMarketplaceRequest) : nextGeneralRequest;
        long waitNanos = Math.max(0, allowedAt - now);
        if (waitNanos > 0) {
            try {
                sleeper.sleep(Duration.ofNanos(waitNanos));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the CardTrader rate limit", exception);
            }
            now = nanoTime.getAsLong();
        }
        nextGeneralRequest = now + generalIntervalNanos;
        if (marketplace) {
            nextMarketplaceRequest = now + marketplaceIntervalNanos;
        }
    }

    private static Duration requireNonNegative(Duration value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isNegative()) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }
}
