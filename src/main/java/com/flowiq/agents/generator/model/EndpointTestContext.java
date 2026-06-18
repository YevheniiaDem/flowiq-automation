package com.flowiq.agents.generator.model;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.generator.schema.JsonSchemaDocument;
import com.flowiq.agents.openapi.OpenApiOperation;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class EndpointTestContext {
    OpenApiOperation operation;
    String normalizedPath;
    String module;
    boolean requiresAuth;
    EndpointCoverage coverage;
    JsonSchemaDocument responseSchema;
    Set<ScenarioType> coveredScenarioTypes;
}
