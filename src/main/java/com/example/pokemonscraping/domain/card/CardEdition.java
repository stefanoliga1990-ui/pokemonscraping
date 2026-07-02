package com.example.pokemonscraping.domain.card;

public enum CardEdition {
    STANDARD,
    FIRST_EDITION,
    SHADOWLESS,
    FIRST_EDITION_SHADOWLESS;

    public static CardEdition from(boolean firstEdition, boolean shadowless) {
        if (firstEdition && shadowless) {
            return FIRST_EDITION_SHADOWLESS;
        }
        if (firstEdition) {
            return FIRST_EDITION;
        }
        if (shadowless) {
            return SHADOWLESS;
        }
        return STANDARD;
    }
}
