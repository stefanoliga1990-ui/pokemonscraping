package com.example.pokemonscraping.parser;

import com.example.pokemonscraping.model.CardOffer;
import com.example.pokemonscraping.util.PriceParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CardMarketOfferParser {

    private final PriceParser priceParser;

    public CardMarketOfferParser(PriceParser priceParser) {
        this.priceParser = priceParser;
    }

    public int countArticleRows(Document document) {
        Elements rows = findRows(document);
        return rows.size();
    }

    public List<CardOffer> extractOffers(Document document) {
        Elements rows = findRows(document);
        List<CardOffer> offers = new ArrayList<>();

        for (Element row : rows) {
            String condition = extractCondition(row);
            String rawPrice = extractPrice(row);

            if (condition == null || rawPrice == null) {
                continue;
            }

            try {
                BigDecimal price = priceParser.parseEuroPrice(rawPrice);
                offers.add(new CardOffer(condition, price));
            } catch (Exception ignored) {
                // in un progetto reale qui potresti loggare la riga scartata
            }
        }

        return offers;
    }

    private Elements findRows(Document document) {
        Elements rows = document.select("div.table-body > div[id^=articleRow]");

        if (rows.isEmpty()) {
            // fallback un po' più permissivo
            rows = document.select("div[id^=articleRow]");
        }

        return rows;
    }

    private String extractCondition(Element row) {
        Element badge = row.selectFirst(".product-attributes .badge");

        if (badge == null) {
            badge = row.selectFirst("a[href*=/CardCondition] .badge");
        }

        return badge != null ? badge.text().trim() : null;
    }

    private String extractPrice(Element row) {
        Element priceElement = row.selectFirst(".price-container span.color-primary");

        if (priceElement == null) {
            priceElement = row.selectFirst(".price-container .color-primary");
        }

        return priceElement != null ? priceElement.text().trim() : null;
    }
}