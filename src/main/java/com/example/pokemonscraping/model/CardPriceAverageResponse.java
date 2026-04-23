package com.example.pokemonscraping.model;

import java.time.LocalDateTime;

public record CardPriceAverageResponse(
        String sourceUrl,
        int totalRowsFound,
        LocalDateTime calculatedAt,
        ConditionAverageResult exResult,
        ConditionAverageResult nmResult
) {
}