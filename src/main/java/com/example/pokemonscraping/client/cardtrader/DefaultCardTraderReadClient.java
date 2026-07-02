package com.example.pokemonscraping.client.cardtrader;

import com.example.pokemonscraping.client.cardtrader.exception.CardTraderAuthenticationException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderHttpException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderRateLimitException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderTransportException;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderBlueprint;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderCategory;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderExpansion;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderMarketplaceOffer;
import com.example.pokemonscraping.config.CardTraderApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class DefaultCardTraderReadClient implements CardTraderReadClient {

    private static final int MAX_ERROR_BODY_LENGTH = 1_000;

    private final CardTraderApiProperties properties;
    private final HttpClient httpClient;
    private final CardTraderResponseParser responseParser;
    private final CardTraderRateLimiter rateLimiter;

    @Autowired
    public DefaultCardTraderReadClient(CardTraderApiProperties properties, ObjectMapper objectMapper) {
        this(
                properties,
                HttpClient.newBuilder()
                        .connectTimeout(properties.getConnectTimeout())
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                new CardTraderResponseParser(objectMapper),
                new CardTraderRateLimiter(
                        properties.getMinimumRequestInterval(),
                        properties.getMarketplaceMinimumRequestInterval()
                )
        );
    }

    DefaultCardTraderReadClient(
            CardTraderApiProperties properties,
            HttpClient httpClient,
            CardTraderResponseParser responseParser,
            CardTraderRateLimiter rateLimiter
    ) {
        this.properties = Objects.requireNonNull(properties);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.responseParser = Objects.requireNonNull(responseParser);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
    }

    @Override
    public List<CardTraderExpansion> getExpansions(long gameId) {
        requirePositive(gameId, "gameId");
        return get("/expansions", Map.of(), false, responseParser::parseExpansions).stream()
                .filter(expansion -> expansion.gameId() == gameId)
                .toList();
    }

    @Override
    public List<CardTraderCategory> getCategories(long gameId) {
        requirePositive(gameId, "gameId");
        return get(
                "/categories",
                Map.of("game_id", Long.toString(gameId)),
                false,
                responseParser::parseCategories
        );
    }

    @Override
    public List<CardTraderBlueprint> getBlueprints(long expansionId) {
        requirePositive(expansionId, "expansionId");
        return get(
                "/blueprints/export",
                Map.of("expansion_id", Long.toString(expansionId)),
                false,
                responseParser::parseBlueprints
        );
    }

    @Override
    public List<CardTraderMarketplaceOffer> getMarketplaceOffersByBlueprint(long blueprintId, String language) {
        requirePositive(blueprintId, "blueprintId");
        Map<Long, List<CardTraderMarketplaceOffer>> offers = get(
                "/marketplace/products",
                marketplaceParameters("blueprint_id", blueprintId, language),
                true,
                responseParser::parseMarketplaceOffers
        );
        return offers.getOrDefault(blueprintId, List.of());
    }

    @Override
    public Map<Long, List<CardTraderMarketplaceOffer>> getMarketplaceOffersByExpansion(
            long expansionId,
            String language
    ) {
        requirePositive(expansionId, "expansionId");
        return get(
                "/marketplace/products",
                marketplaceParameters("expansion_id", expansionId, language),
                true,
                responseParser::parseMarketplaceOffers
        );
    }

    private <T> T get(
            String path,
            Map<String, String> queryParameters,
            boolean marketplace,
            Function<String, T> responseMapper
    ) {
        String token = requireToken();
        rateLimiter.acquire(marketplace);

        URI uri = buildUri(path, queryParameters);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(properties.getReadTimeout())
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CardTraderTransportException("CardTrader request was interrupted: " + path, exception);
        } catch (IOException exception) {
            throw new CardTraderTransportException("CardTrader request failed: " + path, exception);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return responseMapper.apply(response.body());
        }
        throw httpError(status, response);
    }

    private RuntimeException httpError(int status, HttpResponse<String> response) {
        String body = abbreviate(response.body());
        if (status == 401 || status == 403) {
            return new CardTraderAuthenticationException(
                    "CardTrader authentication failed with HTTP " + status + ": " + body
            );
        }
        if (status == 429) {
            return new CardTraderRateLimitException(
                    "CardTrader rate limit exceeded: " + body,
                    retryAfter(response)
            );
        }
        return new CardTraderHttpException(status, "CardTrader returned HTTP " + status + ": " + body);
    }

    private URI buildUri(String path, Map<String, String> queryParameters) {
        URI baseUrl = Objects.requireNonNull(properties.getBaseUrl(), "cardtrader.api.base-url must be configured");
        StringBuilder uri = new StringBuilder(baseUrl.toString().replaceAll("/+$", ""));
        uri.append(path.startsWith("/") ? path : "/" + path);

        if (!queryParameters.isEmpty()) {
            uri.append('?');
            queryParameters.forEach((name, value) -> uri.append(encode(name)).append('=').append(encode(value)).append('&'));
            uri.setLength(uri.length() - 1);
        }
        return URI.create(uri.toString());
    }

    private String requireToken() {
        String token = properties.getToken();
        if (token == null || token.isBlank()) {
            throw new CardTraderAuthenticationException(
                    "Missing CardTrader token: configure the CARDTRADER_API_TOKEN environment variable"
            );
        }
        return token.trim();
    }

    private static Map<String, String> marketplaceParameters(String filterName, long filterId, String language) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put(filterName, Long.toString(filterId));
        if (language != null && !language.isBlank()) {
            String normalizedLanguage = language.trim().toLowerCase(Locale.ROOT);
            if (!normalizedLanguage.matches("[a-z]{2}")) {
                throw new IllegalArgumentException("language must be a two-letter locale code");
            }
            parameters.put("language", normalizedLanguage);
        }
        return parameters;
    }

    private static Duration retryAfter(HttpResponse<String> response) {
        return response.headers().firstValue("Retry-After")
                .flatMap(DefaultCardTraderReadClient::seconds)
                .orElse(Duration.ofSeconds(1));
    }

    private static java.util.Optional<Duration> seconds(String value) {
        try {
            return java.util.Optional.of(Duration.ofSeconds(Long.parseLong(value)));
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }

    private static String abbreviate(String body) {
        if (body == null || body.isBlank()) {
            return "empty response body";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() <= MAX_ERROR_BODY_LENGTH
                ? normalized
                : normalized.substring(0, MAX_ERROR_BODY_LENGTH) + "...";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
