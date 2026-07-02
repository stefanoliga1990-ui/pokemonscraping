package com.example.pokemonscraping.domain.card;

public record CardVariantGroupKey(
        String expansionFamily,
        String cardName,
        String collectorNumber,
        String language
) implements Comparable<CardVariantGroupKey> {

    public String stableValue() {
        return String.join("|", expansionFamily, cardName, collectorNumber, language);
    }

    @Override
    public int compareTo(CardVariantGroupKey other) {
        return stableValue().compareTo(other.stableValue());
    }
}
