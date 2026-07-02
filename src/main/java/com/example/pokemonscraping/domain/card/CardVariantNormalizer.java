package com.example.pokemonscraping.domain.card;

import java.math.BigInteger;
import java.text.Normalizer;
import java.util.Locale;

final class CardVariantNormalizer {

    private CardVariantNormalizer() {
    }

    static String text(String value) {
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    static String collectorNumber(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("\\d+")) {
            return new BigInteger(trimmed).toString();
        }
        return text(trimmed);
    }
}
