package com.example.pokemonscraping.model;

import java.math.BigDecimal;
import java.util.List;

public record ConditionAverageResult(
        String condition,
        int rowsFound,
        List<BigDecimal> prices,
        List<BigDecimal> excludedHighestPrices,
        List<BigDecimal> usedForAveragePrices,
        BigDecimal averagePrice
) {
}