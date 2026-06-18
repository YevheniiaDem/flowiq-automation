package com.flowiq.agents.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.openapi.OpenApiNavigator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnumChangeAnalyzer extends AbstractChangeAnalyzer {

    @Override
    public String name() {
        return "EnumChangeAnalyzer";
    }

    @Override
    protected void doAnalyze(JsonNode previousSpec, JsonNode currentSpec) {
        Map<String, JsonNode> previousSchemas = OpenApiNavigator.getSchemas(previousSpec);
        Map<String, JsonNode> currentSchemas = OpenApiNavigator.getSchemas(currentSpec);

        for (Map.Entry<String, JsonNode> entry : previousSchemas.entrySet()) {
            String schemaName = entry.getKey();
            List<String> previousEnums = OpenApiNavigator.getEnumValues(entry.getValue(), previousSpec);
            if (previousEnums.isEmpty()) {
                continue;
            }
            if (!currentSchemas.containsKey(schemaName)) {
                changes.add(ApiChange.schema(
                        ChangeType.ENUM_REMOVED,
                        schemaName,
                        "Enum schema removed: " + schemaName,
                        true));
                continue;
            }
            List<String> currentEnums = OpenApiNavigator.getEnumValues(currentSchemas.get(schemaName), currentSpec);
            Set<String> removed = new HashSet<>(previousEnums);
            currentEnums.forEach(removed::remove);
            Set<String> added = new HashSet<>(currentEnums);
            previousEnums.forEach(added::remove);

            for (String value : removed) {
                changes.add(ApiChange.field(
                        ChangeType.ENUM_VALUE_REMOVED,
                        schemaName,
                        value,
                        "Enum value removed from " + schemaName + ": " + value));
            }
            for (String value : added) {
                changes.add(ApiChange.field(
                        ChangeType.ENUM_VALUE_ADDED,
                        schemaName,
                        value,
                        "Enum value added to " + schemaName + ": " + value));
            }
        }
    }
}
