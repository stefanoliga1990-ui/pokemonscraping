package com.example.pokemonscraping.client.cardtrader;

import com.example.pokemonscraping.client.cardtrader.exception.CardTraderResponseException;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderBlueprint;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderCategory;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderExpansion;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderMarketplaceOffer;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderMoney;
import com.example.pokemonscraping.client.cardtrader.model.CardTraderProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CardTraderResponseParser {

    private final ObjectMapper objectMapper;

    CardTraderResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    List<CardTraderExpansion> parseExpansions(String body) {
        List<CardTraderExpansion> result = new ArrayList<>();
        for (JsonNode node : catalogueArray(body, "expansions")) {
            result.add(new CardTraderExpansion(
                    requiredLong(node, "id"),
                    requiredLong(node, "game_id"),
                    nullableText(node, "code"),
                    requiredText(node, "name")
            ));
        }
        return List.copyOf(result);
    }

    List<CardTraderCategory> parseCategories(String body) {
        List<CardTraderCategory> result = new ArrayList<>();
        for (JsonNode node : catalogueArray(body, "categories")) {
            result.add(new CardTraderCategory(
                    requiredLong(node, "id"),
                    requiredText(node, "name"),
                    requiredLong(node, "game_id"),
                    parseProperties(node.get("properties"))
            ));
        }
        return List.copyOf(result);
    }

    List<CardTraderBlueprint> parseBlueprints(String body) {
        List<CardTraderBlueprint> result = new ArrayList<>();
        for (JsonNode node : catalogueArray(body, "blueprints")) {
            result.add(new CardTraderBlueprint(
                    requiredLong(node, "id"),
                    requiredText(node, "name"),
                    nullableText(node, "version"),
                    requiredLong(node, "game_id"),
                    requiredLong(node, "category_id"),
                    nullableLong(node, "expansion_id"),
                    nullableText(node, "image_url"),
                    parseProperties(node.get("editable_properties"))
            ));
        }
        return List.copyOf(result);
    }

    Map<Long, List<CardTraderMarketplaceOffer>> parseMarketplaceOffers(String body) {
        JsonNode root = readTree(body);
        if (!root.isObject()) {
            throw invalid("marketplace products", "expected an object indexed by blueprint id");
        }

        Map<Long, List<CardTraderMarketplaceOffer>> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : root.properties()) {
            long blueprintId = numericKey(entry.getKey());
            if (!entry.getValue().isArray()) {
                throw invalid("marketplace products", "value for blueprint " + blueprintId + " is not an array");
            }

            List<CardTraderMarketplaceOffer> offers = new ArrayList<>();
            for (JsonNode node : entry.getValue()) {
                long offerBlueprintId = requiredLong(node, "blueprint_id");
                if (offerBlueprintId != blueprintId) {
                    throw invalid("marketplace products", "blueprint key does not match product blueprint_id");
                }
                offers.add(parseOffer(node));
            }
            result.put(blueprintId, List.copyOf(offers));
        }
        return Map.copyOf(result);
    }

    private CardTraderMarketplaceOffer parseOffer(JsonNode node) {
        JsonNode price = requiredObject(node, "price");
        return new CardTraderMarketplaceOffer(
                requiredLong(node, "id"),
                requiredLong(node, "blueprint_id"),
                requiredText(node, "name_en"),
                requiredInt(node, "quantity"),
                new CardTraderMoney(requiredLong(price, "cents"), requiredText(price, "currency")),
                nullableText(node, "description"),
                parseValueMap(node.get("properties_hash")),
                flexibleBoolean(node.get("graded")),
                flexibleBoolean(node.get("on_vacation")),
                optionalInt(node, "bundle_size", 1)
        );
    }

    private List<CardTraderProperty> parseProperties(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw invalid("property list", "expected an array");
        }

        List<CardTraderProperty> properties = new ArrayList<>();
        for (JsonNode property : node) {
            properties.add(new CardTraderProperty(
                    requiredText(property, "name"),
                    requiredText(property, "type"),
                    scalar(property.get("default_value")),
                    parseValues(property.get("possible_values"))
            ));
        }
        return List.copyOf(properties);
    }

    private List<Object> parseValues(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw invalid("possible_values", "expected an array");
        }
        List<Object> values = new ArrayList<>();
        node.forEach(value -> values.add(scalar(value)));
        return List.copyOf(values);
    }

    private Map<String, Object> parseValueMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }
        if (!node.isObject()) {
            throw invalid("properties_hash", "expected an object");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        node.forEachEntry((name, value) -> values.put(name, scalar(value)));
        return Map.copyOf(values);
    }

    private Object scalar(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isIntegralNumber()) {
            return node.asLong();
        }
        if (node.isFloatingPointNumber()) {
            return node.asDouble();
        }
        if (node.isString()) {
            return node.asString();
        }
        return node.toString();
    }

    private JsonNode catalogueArray(String body, String description) {
        JsonNode root = readTree(body);
        if (root.isArray()) {
            return root;
        }
        if (root.isObject()) {
            JsonNode wrapped = root.get("array");
            if (wrapped == null) {
                wrapped = root.get("data");
            }
            if (wrapped != null && wrapped.isArray()) {
                return wrapped;
            }
        }
        throw invalid(description, "expected an array or an object containing array/data");
    }

    private JsonNode readTree(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (JacksonException exception) {
            throw new CardTraderResponseException("CardTrader returned invalid JSON", exception);
        }
    }

    private static JsonNode requiredObject(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isObject()) {
            throw invalid(field, "expected an object");
        }
        return value;
    }

    private static long requiredLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.canConvertToLong()) {
            throw invalid(field, "expected an integer");
        }
        return value.asLong();
    }

    private static Long nullableLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : requiredLong(node, field);
    }

    private static int requiredInt(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.canConvertToInt()) {
            throw invalid(field, "expected an integer");
        }
        return value.asInt();
    }

    private static int optionalInt(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : requiredInt(node, field);
    }

    private static String requiredText(JsonNode node, String field) {
        String value = nullableText(node, field);
        if (value == null || value.isBlank()) {
            throw invalid(field, "expected non-blank text");
        }
        return value;
    }

    private static String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isString()) {
            throw invalid(field, "expected text");
        }
        return value.asString();
    }

    private static boolean flexibleBoolean(JsonNode value) {
        if (value == null || value.isNull()) {
            return false;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isIntegralNumber()) {
            return value.asInt() != 0;
        }
        if (value.isString()) {
            return Boolean.parseBoolean(value.asString());
        }
        throw invalid("boolean value", "unsupported value");
    }

    private static long numericKey(String key) {
        try {
            return Long.parseLong(key);
        } catch (NumberFormatException exception) {
            throw invalid("marketplace products", "non-numeric blueprint key: " + key);
        }
    }

    private static CardTraderResponseException invalid(String subject, String detail) {
        return new CardTraderResponseException("Invalid CardTrader " + subject + " response: " + detail);
    }
}
