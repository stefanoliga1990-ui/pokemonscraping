package com.example.pokemonscraping.client.cardtrader.exception;

import java.time.Duration;

public class CardTraderRateLimitException extends CardTraderHttpException {

    private final Duration retryAfter;

    public CardTraderRateLimitException(String message, Duration retryAfter) {
        super(429, message);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
