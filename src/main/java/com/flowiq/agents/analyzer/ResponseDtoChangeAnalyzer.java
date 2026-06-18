package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResponseDtoChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "ResponseDtoChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, OpenApiOperation> previous = indexOperations(previousSpec);
        Map<String, OpenApiOperation> current = indexOperations(currentSpec);

        for (String key : previous.keySet()) {
            if (!current.containsKey(key)) {
                continue;
            }
            JsonNode previousResponses = previous.get(key).operation().get("responses");
            JsonNode currentResponses = current.get(key).operation().get("responses");
            if (previousResponses == null || currentResponses == null) {
                continue;
            }
            for (String statusCode : OpenApiNavigator.collectStatusCodes(previousResponses)) {
                if (!currentResponses.has(statusCode)) {
                    continue;
                }
                String previousSchema = extractPrimaryResponseSchema(previousResponses.get(statusCode), previousSpec);
                String currentSchema = extractPrimaryResponseSchema(currentResponses.get(statusCode), currentSpec);
                if (!Objects.equals(previousSchema, currentSchema)) {
                    OpenApiOperation op = current.get(key);
                    changes.add(ApiChange.builder()
                            .type(ChangeType.MODIFIED_RESPONSE_SCHEMA)
                            .method(op.method())
                            .path(op.path())
                            .schema(currentSchema)
                            .field(statusCode)
                            .description("Response schema changed for " + key + " [" + statusCode + "]")
                            .breaking(true)
                            .build());
                }
            }
        }

        compareComponentSchemas(previousSpec, currentSpec);
    }

    private void compareComponentSchemas(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, JsonNode> previousSchemas = OpenApiNavigator.getSchemas(previousSpec);
        Map<String, JsonNode> currentSchemas = OpenApiNavigator.getSchemas(currentSpec);

        for (Map.Entry<String, JsonNode> entry : previousSchemas.entrySet()) {
            String name = entry.getKey();
            if (!currentSchemas.containsKey(name)) {
                changes.add(ApiChange.schema(
                        ChangeType.MODIFIED_RESPONSE_SCHEMA,
                        name,
                        "Schema removed from components: " + name,
                        true));
                continue;
            }
            if (!entry.getValue().equals(currentSchemas.get(name))) {
                boolean propertiesRemoved = hasRemovedProperties(entry.getValue(), currentSchemas.get(name));
                changes.add(ApiChange.schema(
                        ChangeType.MODIFIED_RESPONSE_SCHEMA,
                        name,
                        "Component schema modified: " + name,
                        propertiesRemoved));
            }
        }
    }

    private boolean hasRemovedProperties(JsonNode previous, JsonNode current) {
        if (!previous.has("properties") || !current.has("properties")) {
            return false;
        }
        var previousFields = previous.get("properties").fieldNames();
        while (previousFields.hasNext()) {
            String field = previousFields.next();
            if (!current.get("properties").has(field)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, OpenApiOperation> indexOperations(JsonNode spec) {
        return OpenApiNavigator.getOperations(spec).stream()
                .collect(Collectors.toMap(op -> op.method() + " " + op.path(), Function.identity()));
    }

    private String extractPrimaryResponseSchema(JsonNode response, JsonNode spec) {
        if (response == null) {
            return null;
        }
        JsonNode content = response.get("content");
        if (content == null) {
            return response.toString();
        }
        JsonNode jsonContent = resolveJsonContent(content);
        if (jsonContent == null || !jsonContent.has("schema")) {
            return response.toString();
        }
        JsonNode schema = OpenApiNavigator.resolveRef(spec, jsonContent.get("schema"));
        if (schema != null && schema.has("$ref")) {
            return OpenApiNavigator.schemaNameFromRef(schema.get("$ref").asText());
        }
        return schema != null ? schema.toString() : null;
    }

    private JsonNode resolveJsonContent(JsonNode content) {
        if (content.has("application/json")) {
            return content.get("application/json");
        }
        var iterator = content.elements();
        return iterator.hasNext() ? iterator.next() : null;
    }
}
