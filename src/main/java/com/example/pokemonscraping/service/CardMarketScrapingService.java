package com.example.pokemonscraping.service;

import com.example.pokemonscraping.client.CardMarketHtmlClient;
import com.example.pokemonscraping.model.CardOffer;
import com.example.pokemonscraping.model.CardPriceAverageResponse;
import com.example.pokemonscraping.model.ConditionAverageResult;
import com.example.pokemonscraping.persistence.CardPriceAverageEntity;
import com.example.pokemonscraping.persistence.CardPriceAverageRepository;
import com.example.pokemonscraping.parser.CardMarketOfferParser;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CardMarketScrapingService {

    public static final String DEFAULT_URL =
            "https://www.cardmarket.com/it/Pokemon/Products/Singles/Base-Set/Charizard-V1-BS4?language=5&minCondition=3&isFirstEd=N";

    private final CardMarketHtmlClient htmlClient;
    private final CardMarketOfferParser offerParser;
    private final CardPriceAverageRepository averageRepository;

    public CardMarketScrapingService(CardMarketHtmlClient htmlClient,
                                     CardMarketOfferParser offerParser,
                                     CardPriceAverageRepository averageRepository) {
        this.htmlClient = htmlClient;
        this.offerParser = offerParser;
        this.averageRepository = averageRepository;
    }

    @Transactional
    public CardPriceAverageResponse calculateAverages(String url) throws IOException {
        String effectiveUrl = (url == null || url.isBlank()) ? DEFAULT_URL : url;

        Document document = htmlClient.fetch(effectiveUrl);

        int totalRowsFound = offerParser.countArticleRows(document);
        List<CardOffer> offers = offerParser.extractOffers(document);

        ConditionAverageResult exResult = calculateForCondition(offers, "EX");
        ConditionAverageResult nmResult = calculateForCondition(offers, "NM");

        LocalDateTime calculatedAt = LocalDateTime.now();

        CardPriceAverageEntity entity = new CardPriceAverageEntity();
        entity.setSourceUrl(effectiveUrl);
        entity.setExAveragePrice(exResult.averagePrice());
        entity.setNmAveragePrice(nmResult.averagePrice());
        entity.setCalculatedAt(calculatedAt);

        averageRepository.save(entity);

        return new CardPriceAverageResponse(
                effectiveUrl,
                totalRowsFound,
                calculatedAt,
                exResult,
                nmResult
        );
    }

    public List<CardPriceAverageEntity> getHistory() {
        return averageRepository.findAllByOrderByCalculatedAtAsc();
    }

    private ConditionAverageResult calculateForCondition(List<CardOffer> offers, String condition) {
        List<BigDecimal> prices = offers.stream()
                .filter(offer -> condition.equalsIgnoreCase(offer.condition()))
                .map(CardOffer::price)
                .sorted()
                .toList();

        List<BigDecimal> excludedHighestPrices = prices.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .sorted()
                .toList();

        List<BigDecimal> usedForAveragePrices = prices.stream()
                .sorted(Comparator.reverseOrder())
                .skip(3)
                .sorted()
                .toList();

        BigDecimal averagePrice = calculateAverage(usedForAveragePrices);

        return new ConditionAverageResult(
                condition,
                prices.size(),
                prices,
                excludedHighestPrices,
                usedForAveragePrices,
                averagePrice
        );
    }

    private BigDecimal calculateAverage(List<BigDecimal> prices) {
        if (prices == null || prices.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal sum = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
    }
}