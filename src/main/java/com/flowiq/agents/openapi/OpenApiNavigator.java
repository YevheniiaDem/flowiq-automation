package com.flowiq.agents.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OpenApiNavigator {

    private static final Set<String> HTTP_METHODS = Set.of(
            "get", "post", "put", "patch", "delete", "head", "options", "trace"
    );

    private OpenApiNavigator() {
    }

    public static List<OpenApiOperation> getOperations(JsonNode spec) {
        if (spec == null) {
            return List.of();
        }
        JsonNode pathsNode = spec.path("paths");
        if (!pathsNode.isObject()) {
            return List.of();
        }
        List<OpenApiOperation> operations = new ArrayList<>();
        Iterator<String> pathNames = pathsNode.fieldNames();
        while (pathNames.hasNext()) {
            String path = pathNames.next();
            JsonNode pathItem = pathsNode.get(path);
            for (String method : HTTP_METHODS) {
                if (pathItem.has(method)) {
                    operations.add(new OpenApiOperation(path, method.toUpperCase(), pathItem.get(method)));
                }
            }
        }
        return operations;
    }

    public static Map<String, JsonNode> getSchemas(JsonNode spec) {
        if (spec == null) {
            return Map.of();
        }
        JsonNode schemasNode = spec.path("components").path("schemas");
        if (!schemasNode.isObject()) {
            return Map.of();
        }
        Map<String, JsonNode> schemas = new LinkedHashMap<>();
        Iterator<String> schemaNames = schemasNode.fieldNames();
        while (schemaNames.hasNext()) {
            String name = schemaNames.next();
            schemas.put(name, schemasNode.get(name));
        }
        return schemas;
    }

    public static JsonNode resolveRef(JsonNode spec, JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.has("$ref")) {
            return resolveRefString(spec, node.get("$ref").asText());
        }
        return node;
    }

    public static JsonNode resolveRefString(JsonNode spec, String ref) {
        if (ref == null || !ref.startsWith("#/")) {
            return null;
        }
        String[] parts = ref.substring(2).split("/");
        JsonNode current = spec;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    public static String schemaNameFromRef(String ref) {
        if (ref == null) {
            return null;
        }
        int idx = ref.lastIndexOf('/');
        return idx >= 0 ? ref.substring(idx + 1) : ref;
    }

    public static List<String> getRequiredFields(JsonNode schema, JsonNode spec) {
        JsonNode resolved = resolveRef(spec, schema);
        if (resolved == null || !resolved.has("required")) {
            return List.of();
        }
        List<String> required = new ArrayList<>();
        resolved.get("required").forEach(field -> required.add(field.asText()));
        return required;
    }

    public static List<String> getEnumValues(JsonNode schema, JsonNode spec) {
        JsonNode resolved = resolveRef(spec, schema);
        if (resolved == null || !resolved.has("enum")) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        resolved.get("enum").forEach(value -> values.add(value.asText()));
        return values;
    }

    public static Set<String> collectStatusCodes(JsonNode operation) {
        if (operation == null || !operation.has("responses")) {
            return Set.of();
        }
        Set<String> codes = new LinkedHashSet<>();
        operation.get("responses").fieldNames().forEachRemaining(codes::add);
        return codes;
    }

    public static String normalizeSpec(JsonNode spec, ObjectMapper mapper) {
        try {
            ObjectNode copy = spec.deepCopy();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(copy);
        } catch (Exception e) {
            return spec.toString();
        }
    }

    public static Map<String, JsonNode> indexSchemasByUsage(JsonNode spec) {
        Map<String, JsonNode> schemas = getSchemas(spec);
        return schemas.isEmpty() ? Collections.emptyMap() : schemas;
    }
}
