package com.example.pokemonscraping.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CardPriceAverageResponse(
        String sourceUrl,
        int totalRowsFound,
        int exRowsFound,
        List<BigDecimal> exPrices,
        List<BigDecimal> excludedHighestPrices,
        List<BigDecimal> usedForAveragePrices,
        BigDecimal averagePrice,
        LocalDateTime calculatedAt
) {
}