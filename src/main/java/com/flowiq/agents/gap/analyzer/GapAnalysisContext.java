package com.flowiq.agents.gap.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.openapi.OpenApiOperation;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class GapAnalysisContext {
    JsonNode openApiSpec;
  @Singular
    List<OpenApiOperation> operations;
  @Singular
    List<ScannedTestReference> testReferences;
  @Singular
    List<EndpointCoverage> endpointCoverages;
    Map<String, GapSeverity> moduleBusinessImpact;
    Set<String> uiExpectedModules;
}
