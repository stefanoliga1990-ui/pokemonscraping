package com.example.pokemonscraping.domain.card;

import java.util.regex.Pattern;

public final class ExpansionVariantClassifier {

    private static final Pattern SHADOWLESS_SUFFIX = Pattern.compile(
            "(?i)\\s*(?:-\\s*)?(?:\\(\\s*)?shadowless(?:\\s*\\))?\\s*$"
    );

    private ExpansionVariantClassifier() {
    }

    public static boolean isShadowless(String expansionName) {
        return SHADOWLESS_SUFFIX.matcher(expansionName.trim()).find();
    }

    public static String familyName(String expansionName) {
        String trimmed = expansionName.trim();
        String withoutSuffix = SHADOWLESS_SUFFIX.matcher(trimmed).replaceFirst("").trim();
        return withoutSuffix.isEmpty() ? trimmed : withoutSuffix;
    }
}
