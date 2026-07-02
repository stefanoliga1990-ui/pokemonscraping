package com.example.pokemonscraping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "cardtrader.api")
public class CardTraderApiProperties {

    private URI baseUrl;
    private String token;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration minimumRequestInterval;
    private Duration marketplaceMinimumRequestInterval;

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getMinimumRequestInterval() {
        return minimumRequestInterval;
    }

    public void setMinimumRequestInterval(Duration minimumRequestInterval) {
        this.minimumRequestInterval = minimumRequestInterval;
    }

    public Duration getMarketplaceMinimumRequestInterval() {
        return marketplaceMinimumRequestInterval;
    }

    public void setMarketplaceMinimumRequestInterval(Duration marketplaceMinimumRequestInterval) {
        this.marketplaceMinimumRequestInterval = marketplaceMinimumRequestInterval;
    }
}
