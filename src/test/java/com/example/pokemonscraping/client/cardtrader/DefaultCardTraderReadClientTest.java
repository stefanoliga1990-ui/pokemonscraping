package com.example.pokemonscraping.client.cardtrader;

import com.example.pokemonscraping.client.cardtrader.exception.CardTraderAuthenticationException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderHttpException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderRateLimitException;
import com.example.pokemonscraping.client.cardtrader.exception.CardTraderTransportException;
import com.example.pokemonscraping.config.CardTraderApiProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultCardTraderReadClientTest {

    private HttpServer server;
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> query = new AtomicReference<>();
    private volatile int responseStatus = 200;
    private volatile String responseBody = "[]";
    private volatile String retryAfter;
    private volatile Duration responseDelay = Duration.ZERO;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v2", this::respond);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void sendsBearerTokenAndParsesMarketplaceProducts() {
        responseBody = fixture("marketplace-products.json");

        var offers = client(Duration.ofSeconds(2)).getMarketplaceOffersByBlueprint(111151, "IT");

        assertThat(offers).hasSize(2);
        assertThat(authorization.get()).isEqualTo("Bearer test-token");
        assertThat(query.get()).contains("blueprint_id=111151", "language=it");
    }

    @Test
    void filtersExpansionsByGameId() {
        responseBody = fixture("expansions.json");

        var expansions = client(Duration.ofSeconds(2)).getExpansions(5);

        assertThat(expansions).extracting("name").containsExactly("Base Set", "Base Set Shadowless");
    }

    @Test
    void mapsAuthenticationAndGenericHttpErrors() {
        responseStatus = 401;
        responseBody = "{\"error\":\"invalid token\"}";
        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).getCategories(5))
                .isInstanceOf(CardTraderAuthenticationException.class)
                .hasMessageContaining("invalid token");

        responseStatus = 503;
        responseBody = "{\"error\":\"maintenance\"}";
        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).getCategories(5))
                .isInstanceOf(CardTraderHttpException.class)
                .extracting(exception -> ((CardTraderHttpException) exception).getStatusCode())
                .isEqualTo(503);
    }

    @Test
    void exposesRetryAfterForRateLimitResponses() {
        responseStatus = 429;
        responseBody = "{\"error\":\"Too many requests\"}";
        retryAfter = "7";

        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).getCategories(5))
                .isInstanceOfSatisfying(CardTraderRateLimitException.class, exception ->
                        assertThat(exception.getRetryAfter()).isEqualTo(Duration.ofSeconds(7))
                );
    }

    @Test
    void failsFastWhenTokenIsMissing() {
        CardTraderApiProperties properties = properties(Duration.ofSeconds(2));
        properties.setToken(" ");

        assertThatThrownBy(() -> client(properties).getCategories(5))
                .isInstanceOf(CardTraderAuthenticationException.class)
                .hasMessageContaining("CARDTRADER_API_TOKEN");
    }

    @Test
    void appliesReadTimeout() {
        responseDelay = Duration.ofMillis(250);

        assertThatThrownBy(() -> client(Duration.ofMillis(50)).getCategories(5))
                .isInstanceOf(CardTraderTransportException.class);
    }

    private DefaultCardTraderReadClient client(Duration readTimeout) {
        return client(properties(readTimeout));
    }

    private DefaultCardTraderReadClient client(CardTraderApiProperties properties) {
        return new DefaultCardTraderReadClient(
                properties,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                new CardTraderResponseParser(new ObjectMapper()),
                new CardTraderRateLimiter(Duration.ZERO, Duration.ZERO)
        );
    }

    private CardTraderApiProperties properties(Duration readTimeout) {
        CardTraderApiProperties properties = new CardTraderApiProperties();
        properties.setBaseUrl(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v2"));
        properties.setToken("test-token");
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(readTimeout);
        properties.setMinimumRequestInterval(Duration.ZERO);
        properties.setMarketplaceMinimumRequestInterval(Duration.ZERO);
        return properties;
    }

    private void respond(HttpExchange exchange) throws IOException {
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        query.set(exchange.getRequestURI().getRawQuery());
        if (!responseDelay.isZero()) {
            try {
                Thread.sleep(responseDelay.toMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        if (retryAfter != null) {
            exchange.getResponseHeaders().add("Retry-After", retryAfter);
        }
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(responseStatus, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static String fixture(String name) {
        try {
            return new ClassPathResource("fixtures/cardtrader/" + name)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
