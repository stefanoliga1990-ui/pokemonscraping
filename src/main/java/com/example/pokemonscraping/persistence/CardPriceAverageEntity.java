package com.example.pokemonscraping.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_price_average")
public class CardPriceAverageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    @Column(name = "ex_average_price", precision = 19, scale = 2)
    private BigDecimal exAveragePrice;

    @Column(name = "nm_average_price", precision = 19, scale = 2)
    private BigDecimal nmAveragePrice;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public Long getId() {
        return id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public BigDecimal getExAveragePrice() {
        return exAveragePrice;
    }

    public BigDecimal getNmAveragePrice() {
        return nmAveragePrice;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setExAveragePrice(BigDecimal exAveragePrice) {
        this.exAveragePrice = exAveragePrice;
    }

    public void setNmAveragePrice(BigDecimal nmAveragePrice) {
        this.nmAveragePrice = nmAveragePrice;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}