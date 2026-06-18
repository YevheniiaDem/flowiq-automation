package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatusCodeChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "StatusCodeChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, OpenApiOperation> previous = indexOperations(previousSpec);
        Map<String, OpenApiOperation> current = indexOperations(currentSpec);

        for (Map.Entry<String, OpenApiOperation> entry : previous.entrySet()) {
            String key = entry.getKey();
            if (!current.containsKey(key)) {
                continue;
            }
            Set<String> previousCodes = OpenApiNavigator.collectStatusCodes(entry.getValue().operation());
            Set<String> currentCodes = OpenApiNavigator.collectStatusCodes(current.get(key).operation());

            for (String code : previousCodes) {
                if (!currentCodes.contains(code)) {
                    OpenApiOperation op = entry.getValue();
                    changes.add(ApiChange.builder()
                            .type(ChangeType.STATUS_CODE_REMOVED)
                            .method(op.method())
                            .path(op.path())
                            .field(code)
                            .description("Status code removed: " + op.method() + " " + op.path() + " -> " + code)
                            .breaking(isSuccessCode(code))
                            .build());
                }
            }
            for (String code : currentCodes) {
                if (!previousCodes.contains(code)) {
                    OpenApiOperation op = current.get(key);
                    changes.add(ApiChange.builder()
                            .type(ChangeType.STATUS_CODE_ADDED)
                            .method(op.method())
                            .path(op.path())
                            .field(code)
                            .description("Status code added: " + op.method() + " " + op.path() + " -> " + code)
                            .breaking(false)
                            .build());
                }
            }
        }
    }

    private boolean isSuccessCode(String code) {
        return code.startsWith("2");
    }

    private Map<String, OpenApiOperation> indexOperations(JsonNode spec) {
        return OpenApiNavigator.getOperations(spec).stream()
                .collect(Collectors.toMap(op -> op.method() + " " + op.path(), Function.identity()));
    }
}
