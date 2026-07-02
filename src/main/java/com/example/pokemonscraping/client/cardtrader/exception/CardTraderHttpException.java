package com.example.pokemonscraping.client.cardtrader.exception;

public class CardTraderHttpException extends CardTraderClientException {

    private final int statusCode;

    public CardTraderHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
