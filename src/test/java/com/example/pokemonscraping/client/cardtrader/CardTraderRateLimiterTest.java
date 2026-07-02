package com.example.pokemonscraping.client.cardtrader;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class CardTraderRateLimiterTest {

    @Test
    void appliesTheMoreRestrictiveMarketplaceInterval() {
        AtomicLong now = new AtomicLong();
        List<Duration> waits = new ArrayList<>();
        CardTraderRateLimiter limiter = new CardTraderRateLimiter(
                Duration.ofMillis(50),
                Duration.ofSeconds(1),
                now::get,
                duration -> {
                    waits.add(duration);
                    now.addAndGet(duration.toNanos());
                }
        );

        limiter.acquire(true);
        limiter.acquire(true);
        limiter.acquire(false);

        assertThat(waits).containsExactly(Duration.ofSeconds(1), Duration.ofMillis(50));
    }
}
