package com.example.pokemonscraping.client.cardtrader.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record CardTraderMarketplaceOffer(
        long id,
        long blueprintId,
        String name,
        int quantity,
        CardTraderMoney price,
        String description,
        Map<String, Object> properties,
        boolean graded,
        boolean onVacation,
        int bundleSize
) {

    public CardTraderMarketplaceOffer {
        properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }
}
