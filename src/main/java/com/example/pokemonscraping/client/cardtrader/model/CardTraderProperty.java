package com.example.pokemonscraping.client.cardtrader.model;

import java.util.List;

public record CardTraderProperty(String name, String type, Object defaultValue, List<Object> possibleValues) {

    public CardTraderProperty {
        possibleValues = List.copyOf(possibleValues);
    }
}
