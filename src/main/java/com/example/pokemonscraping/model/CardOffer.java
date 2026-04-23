package com.example.pokemonscraping.model;

import java.math.BigDecimal;

public record CardOffer(
        String condition,
        BigDecimal price
) {
}