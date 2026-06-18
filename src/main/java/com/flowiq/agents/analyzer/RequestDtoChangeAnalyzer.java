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

public class RequestDtoChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "RequestDtoChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, OpenApiOperation> previous = indexOperations(previousSpec);
        Map<String, OpenApiOperation> current = indexOperations(currentSpec);

        for (String key : previous.keySet()) {
            if (!current.containsKey(key)) {
                continue;
            }
            JsonNode previousBody = previous.get(key).operation().get("requestBody");
            JsonNode currentBody = current.get(key).operation().get("requestBody");
            if (Objects.equals(previousBody, currentBody)) {
                continue;
            }
            if (previousBody == null && currentBody != null) {
                changes.add(ApiChange.endpoint(
                        ChangeType.MODIFIED_REQUEST_SCHEMA,
                        current.get(key).method(),
                        current.get(key).path(),
                        "Request body added to " + key));
                continue;
            }
            if (previousBody != null && currentBody == null) {
                changes.add(ApiChange.endpoint(
                        ChangeType.MODIFIED_REQUEST_SCHEMA,
                        previous.get(key).method(),
                        previous.get(key).path(),
                        "Request body removed from " + key));
                continue;
            }
            String previousSchema = extractSchemaRef(previousBody, previousSpec);
            String currentSchema = extractSchemaRef(currentBody, currentSpec);
            if (!Objects.equals(previousSchema, currentSchema)) {
                OpenApiOperation op = current.get(key);
                changes.add(ApiChange.builder()
                        .type(ChangeType.MODIFIED_REQUEST_SCHEMA)
                        .method(op.method())
                        .path(op.path())
                        .schema(currentSchema)
                        .description("Request DTO changed for " + key
                                + ": " + previousSchema + " -> " + currentSchema)
                        .breaking(true)
                        .build());
            }
        }
    }

    private Map<String, OpenApiOperation> indexOperations(JsonNode spec) {
        return OpenApiNavigator.getOperations(spec).stream()
                .collect(Collectors.toMap(op -> op.method() + " " + op.path(), Function.identity()));
    }

    private String extractSchemaRef(JsonNode requestBody, JsonNode spec) {
        if (requestBody == null) {
            return null;
        }
        JsonNode content = requestBody.get("content");
        if (content == null) {
            return requestBody.toString();
        }
        JsonNode jsonContent = resolveJsonContent(content);
        if (jsonContent == null || !jsonContent.has("schema")) {
            return requestBody.toString();
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
