package com.mentify.tests.contracts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

final class ContractAssertions {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SENSITIVE_FIELDS =
            "password,access_token,refresh_token,api_key,secret,stacktrace,internal_url";

    private ContractAssertions() {
    }

    static JsonNode jsonObject(Response response) {
        String body = response.asString();
        try {
            JsonNode root = MAPPER.readTree(body);
            assertTrue(root != null && root.isObject(),
                    "Contract response must be a JSON object");
            return root;
        } catch (Exception exception) {
            throw new AssertionError("Contract response is not valid JSON", exception);
        }
    }

    static void assertCommonResponse(Response response) {
        JsonNode root = jsonObject(response);
        assertOptionalType(root, "status", JsonNode::isTextual);
        assertOptionalType(root, "message", JsonNode::isTextual);
        assertNoSensitiveFields(root);
    }

    static void assertErrorResponse(Response response) {
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600,
                "Contract error response must use an error status");
        JsonNode root = jsonObject(response);
        assertOptionalType(root, "status", JsonNode::isTextual);
        assertOptionalType(root, "statusCode", JsonNode::isIntegralNumber);
        assertOptionalType(root, "message", JsonNode::isTextual);
        assertOptionalType(root, "timestamp", ContractAssertions::isDateTime);
        assertNoSensitiveFields(root);
    }

    static void assertNoSensitiveFields(JsonNode root) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String name = field.getKey().toLowerCase(Locale.ROOT);
            assertFalse(SENSITIVE_FIELDS.contains(name),
                    "Contract must not expose sensitive field: " + field.getKey());
            if (field.getValue().isObject()) {
                assertNoSensitiveFields(field.getValue());
            } else if (field.getValue().isArray()) {
                field.getValue().forEach(ContractAssertions::assertNoSensitiveFields);
            }
        }
    }

    private static void assertOptionalType(
            JsonNode root, String name, java.util.function.Predicate<JsonNode> type) {
        if (root.has(name) && !root.get(name).isNull()) {
            assertTrue(type.test(root.get(name)), "Contract field has incompatible type: " + name);
        }
    }

    private static boolean isDateTime(JsonNode node) {
        if (!node.isTextual()) {
            return false;
        }
        try {
            OffsetDateTime.parse(node.asText());
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }
}
