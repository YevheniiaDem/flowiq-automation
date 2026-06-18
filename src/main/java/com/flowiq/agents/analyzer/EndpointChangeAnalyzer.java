package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EndpointChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "EndpointChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, OpenApiOperation> previous = index(OpenApiNavigator.getOperations(previousSpec));
        Map<String, OpenApiOperation> current = index(OpenApiNavigator.getOperations(currentSpec));

        for (Map.Entry<String, OpenApiOperation> entry : previous.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                OpenApiOperation op = entry.getValue();
                changes.add(ApiChange.endpoint(
                        ChangeType.REMOVED_ENDPOINT,
                        op.method(),
                        op.path(),
                        "Endpoint removed: " + op.method() + " " + op.path()));
            }
        }

        for (Map.Entry<String, OpenApiOperation> entry : current.entrySet()) {
            if (!previous.containsKey(entry.getKey())) {
                OpenApiOperation op = entry.getValue();
                changes.add(ApiChange.endpoint(
                        ChangeType.ADDED_ENDPOINT,
                        op.method(),
                        op.path(),
                        "Endpoint added: " + op.method() + " " + op.path()));
            }
        }
    }

    private Map<String, OpenApiOperation> index(java.util.List<OpenApiOperation> operations) {
        return operations.stream()
                .collect(Collectors.toMap(
                        op -> op.method() + " " + op.path(),
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }
}
