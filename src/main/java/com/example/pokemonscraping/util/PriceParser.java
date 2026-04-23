package com.example.pokemonscraping.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PriceParser {

    public BigDecimal parseEuroPrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            throw new IllegalArgumentException("Prezzo nullo o vuoto");
        }

        String normalized = rawPrice
                .replace("\u00A0", "")   // spazio non-breaking
                .replace("€", "")
                .replace(".", "")        // rimuove separatore migliaia
                .replace(",", ".")       // converte decimale italiano in formato Java
                .replaceAll("[^\\d.\\-]", "")
                .trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Prezzo non valido: " + rawPrice);
        }

        return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
    }
}