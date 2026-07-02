package com.example.pokemonscraping.client.cardtrader.model;

import java.util.List;

public record CardTraderBlueprint(
        long id,
        String name,
        String version,
        long gameId,
        long categoryId,
        Long expansionId,
        String imageUrl,
        List<CardTraderProperty> editableProperties
) {

    public CardTraderBlueprint {
        editableProperties = List.copyOf(editableProperties);
    }
}
