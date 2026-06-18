package com.flowiq.agents.generator.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class ScenarioGenerationResult {
    Instant generatedAt;
    int endpointsAnalyzed;
    int schemasLoaded;
    int existingTestReferences;
  @Singular
    List<TestScenario> scenarios;
    Map<ScenarioType, Long> scenariosByType;
    String dataSourcesSummary;
}
