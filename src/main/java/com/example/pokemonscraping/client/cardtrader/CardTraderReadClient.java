package com.example.pokemonscraping.client.cardtrader;

import com.example.pokemonscraping.client.cardtrader.model.CardTraderBlueprint;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderCategory;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderExpansion;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderMarketplaceOffer;

import java.util.List;
import java.util.Map;

public interface CardTraderReadClient {

    List<CardTraderExpansion> getExpansions(long gameId);

    List<CardTraderCategory> getCategories(long gameId);

    List<CardTraderBlueprint> getBlueprints(long expansionId);

    List<CardTraderMarketplaceOffer> getMarketplaceOffersByBlueprint(long blueprintId, String language);

    Map<Long, List<CardTraderMarketplaceOffer>> getMarketplaceOffersByExpansion(long expansionId, String language);
}
