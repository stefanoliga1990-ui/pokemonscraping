package com.example.pokemonscraping.domain.card;

import java.util.List;

public record CardVariantGroup(
        CardVariantGroupKey key,
        String expansionFamilyName,
        String cardName,
        String collectorNumber,
        String language,
        List<CardVariant> variants
) {

    public CardVariantGroup {
        variants = List.copyOf(variants);
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("A variant group must contain at least one variant");
        }
    }

    public boolean isAmbiguous() {
        return variants.size() > 1;
    }

    public List<CardVariantOption> options() {
        return CardVariantLabelFormatter.optionsFor(this);
    }
}
