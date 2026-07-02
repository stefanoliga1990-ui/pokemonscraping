package com.example.pokemonscraping.domain.card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class CardVariantLabelFormatter {

    private CardVariantLabelFormatter() {
    }

    static List<CardVariantOption> optionsFor(CardVariantGroup group) {
        boolean hasEditionAlternatives = group.variants().stream()
                .anyMatch(variant -> variant.firstEdition() || variant.shadowless());

        Map<CardVariant, String> labels = group.variants().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        variant -> label(variant, hasEditionAlternatives)
                ));
        Map<String, Long> occurrences = labels.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return group.variants().stream()
                .map(variant -> new CardVariantOption(
                        variant,
                        uniqueLabel(variant, labels.get(variant), occurrences)
                ))
                .toList();
    }

    private static String uniqueLabel(
            CardVariant variant,
            String label,
            Map<String, Long> occurrences
    ) {
        if (occurrences.getOrDefault(label, 0L) <= 1) {
            return label;
        }
        return label + " · Variant " + variant.blueprintId();
    }

    private static String label(CardVariant variant, boolean hasEditionAlternatives) {
        List<String> parts = new ArrayList<>();

        switch (variant.edition()) {
            case FIRST_EDITION -> parts.add("First Edition");
            case SHADOWLESS -> parts.add("Shadowless");
            case FIRST_EDITION_SHADOWLESS -> parts.add("First Edition Shadowless");
            case STANDARD -> {
                if (hasEditionAlternatives) {
                    parts.add("Unlimited");
                }
            }
        }

        if (variant.blueprintVersion() != null) {
            parts.add(variant.blueprintVersion());
        }
        if (variant.reverse()) {
            parts.add("Reverse");
        }
        if (variant.graded()) {
            parts.add("Graded");
        }
        if (variant.signed()) {
            parts.add("Signed");
        }
        if (variant.altered()) {
            parts.add("Altered");
        }

        return parts.isEmpty() ? "Standard" : String.join(" · ", parts);
    }
}
