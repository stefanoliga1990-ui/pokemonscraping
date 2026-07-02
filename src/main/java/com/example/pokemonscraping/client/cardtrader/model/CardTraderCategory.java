package com.example.pokemonscraping.client.cardtrader.model;

import java.util.List;

public record CardTraderCategory(long id, String name, long gameId, List<CardTraderProperty> properties) {

    public CardTraderCategory {
        properties = List.copyOf(properties);
    }
}
