package com.example.pokemonscraping.client.cardtrader.exception;

public class CardTraderResponseException extends CardTraderClientException {

    public CardTraderResponseException(String message) {
        super(message);
    }

    public CardTraderResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
