package com.gestiva.billing.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static JsonNode readTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON non valido", ex);
        }
    }

    public static String readText(String json, String pointer) {
        try {
            JsonNode node = MAPPER.readTree(json).at(pointer);
            return node.isMissingNode() || node.isNull() ? null : node.asText();
        } catch (Exception ex) {
            return null;
        }
    }
}
