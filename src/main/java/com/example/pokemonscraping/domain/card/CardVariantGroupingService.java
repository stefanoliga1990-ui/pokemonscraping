package com.example.pokemonscraping.domain.card;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CardVariantGroupingService {

    private static final Comparator<CardVariant> VARIANT_ORDER = Comparator
            .comparing(CardVariant::edition)
            .thenComparing(CardVariant::reverse)
            .thenComparing(CardVariant::graded)
            .thenComparing(CardVariant::signed)
            .thenComparing(CardVariant::altered)
            .thenComparing(CardVariant::blueprintId)
            .thenComparing(variant -> Objects.toString(variant.blueprintVersion(), ""));

    public List<CardVariantGroup> group(Collection<CardVariant> candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");

        Map<CardVariantGroupKey, List<CardVariant>> grouped = candidates.stream()
                .map(this::withClassifiedExpansion)
                .distinct()
                .collect(Collectors.groupingBy(
                        this::groupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toGroup(entry.getKey(), entry.getValue()))
                .toList();
    }

    private CardVariant withClassifiedExpansion(CardVariant variant) {
        boolean shadowless = variant.shadowless()
                || ExpansionVariantClassifier.isShadowless(variant.expansionName());

        if (shadowless == variant.shadowless()) {
            return variant;
        }

        return new CardVariant(
                variant.blueprintId(),
                variant.expansionId(),
                variant.expansionName(),
                variant.cardName(),
                variant.collectorNumber(),
                variant.blueprintVersion(),
                variant.language(),
                variant.firstEdition(),
                true,
                variant.reverse(),
                variant.graded(),
                variant.signed(),
                variant.altered()
        );
    }

    private CardVariantGroupKey groupKey(CardVariant variant) {
        String family = ExpansionVariantClassifier.familyName(variant.expansionName());
        return new CardVariantGroupKey(
                CardVariantNormalizer.text(family),
                CardVariantNormalizer.text(variant.cardName()),
                CardVariantNormalizer.collectorNumber(variant.collectorNumber()),
                variant.language()
        );
    }

    private CardVariantGroup toGroup(CardVariantGroupKey key, List<CardVariant> variants) {
        List<CardVariant> sortedVariants = variants.stream().sorted(VARIANT_ORDER).toList();
        CardVariant representative = sortedVariants.get(0);

        return new CardVariantGroup(
                key,
                ExpansionVariantClassifier.familyName(representative.expansionName()),
                representative.cardName(),
                representative.collectorNumber(),
                representative.language(),
                sortedVariants
        );
    }
}
