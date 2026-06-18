package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.openapi.OpenApiNavigator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequiredFieldChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "RequiredFieldChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, JsonNode> previousSchemas = OpenApiNavigator.getSchemas(previousSpec);
        Map<String, JsonNode> currentSchemas = OpenApiNavigator.getSchemas(currentSpec);

        for (Map.Entry<String, JsonNode> entry : previousSchemas.entrySet()) {
            String schemaName = entry.getKey();
            if (!currentSchemas.containsKey(schemaName)) {
                continue;
            }
            List<String> previousRequired = OpenApiNavigator.getRequiredFields(entry.getValue(), previousSpec);
            List<String> currentRequired = OpenApiNavigator.getRequiredFields(currentSchemas.get(schemaName), currentSpec);

            Set<String> added = new HashSet<>(currentRequired);
            previousRequired.forEach(added::remove);
            Set<String> removed = new HashSet<>(previousRequired);
            currentRequired.forEach(removed::remove);

            for (String field : added) {
                changes.add(ApiChange.field(
                        ChangeType.ADDED_REQUIRED_FIELD,
                        schemaName,
                        field,
                        "Required field added to " + schemaName + ": " + field));
            }
            for (String field : removed) {
                changes.add(ApiChange.field(
                        ChangeType.REMOVED_REQUIRED_FIELD,
                        schemaName,
                        field,
                        "Required field removed from " + schemaName + ": " + field));
            }
        }
    }
}
