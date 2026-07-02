package com.example.pokemonscraping.client.cardtrader.exception;

public class CardTraderClientException extends RuntimeException {

    public CardTraderClientException(String message) {
        super(message);
    }

    public CardTraderClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
