package com.example.pokemonscraping.domain.card;

import java.util.Locale;
import java.util.Objects;

public record CardVariant(
        long blueprintId,
        long expansionId,
        String expansionName,
        String cardName,
        String collectorNumber,
        String blueprintVersion,
        String language,
        boolean firstEdition,
        boolean shadowless,
        boolean reverse,
        boolean graded,
        boolean signed,
        boolean altered
) {

    public CardVariant {
        if (blueprintId <= 0) {
            throw new IllegalArgumentException("blueprintId must be positive");
        }
        if (expansionId <= 0) {
            throw new IllegalArgumentException("expansionId must be positive");
        }
        expansionName = requireText(expansionName, "expansionName");
        cardName = requireText(cardName, "cardName");
        collectorNumber = requireText(collectorNumber, "collectorNumber");
        language = requireText(language, "language").toLowerCase(Locale.ROOT);
        blueprintVersion = normalizeOptional(blueprintVersion);
    }

    public CardEdition edition() {
        return CardEdition.from(firstEdition, shadowless);
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
