package com.example.pokemonscraping.client.cardtrader;

import com.example.pokemonscraping.client.cardtrader.exception.CardTraderResponseException;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderMarketplaceOffer;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTraderResponseParserTest {

    private final CardTraderResponseParser parser = new CardTraderResponseParser(new ObjectMapper());

    @Test
    void parsesDirectExpansionArray() {
        var expansions = parser.parseExpansions(fixture("expansions.json"));

        assertThat(expansions).hasSize(3);
        assertThat(expansions.get(0).id()).isEqualTo(1472);
        assertThat(expansions.get(0).gameId()).isEqualTo(5);
        assertThat(expansions.get(0).name()).isEqualTo("Base Set");
    }

    @Test
    void parsesCatalogueWrappersAndTypedProperties() {
        var categories = parser.parseCategories(fixture("categories-wrapped.json"));
        var blueprints = parser.parseBlueprints(fixture("blueprints-data.json"));

        assertThat(categories).singleElement().satisfies(category -> {
            assertThat(category.id()).isEqualTo(73);
            assertThat(category.properties()).hasSize(2);
            assertThat(category.properties().get(1).defaultValue()).isEqualTo(false);
            assertThat(category.properties().get(1).possibleValues()).containsExactly(true, false);
        });
        assertThat(blueprints).singleElement().satisfies(blueprint -> {
            assertThat(blueprint.id()).isEqualTo(111151);
            assertThat(blueprint.expansionId()).isEqualTo(1472);
            assertThat(blueprint.version()).isEqualTo("Holo Rare | 4/102");
        });
    }

    @Test
    void parsesMarketplaceObjectWithNumericBlueprintKeys() {
        Map<Long, List<CardTraderMarketplaceOffer>> products =
                parser.parseMarketplaceOffers(fixture("marketplace-products.json"));

        assertThat(products).containsOnlyKeys(111151L, 222222L);
        assertThat(products.get(222222L)).isEmpty();
        assertThat(products.get(111151L)).hasSize(2);
        assertThat(products.get(111151L).get(0).price().cents()).isEqualTo(32999);
        assertThat(products.get(111151L).get(0).properties())
                .containsEntry("condition", "Near Mint")
                .containsEntry("first_edition", false);
        assertThat(products.get(111151L).get(1).graded()).isTrue();
    }

    @Test
    void rejectsNonNumericMarketplaceKeys() {
        assertThatThrownBy(() -> parser.parseMarketplaceOffers("{\"not-a-number\": []}"))
                .isInstanceOf(CardTraderResponseException.class)
                .hasMessageContaining("non-numeric blueprint key");
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
