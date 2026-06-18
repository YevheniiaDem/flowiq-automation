package com.flowiq.agents.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class AnalysisResult {
    Instant analyzedAt;
  @Singular
    List<ApiChange> changes;
    RiskLevel riskLevel;
  @Singular
    Map<TestSuiteType, List<String>> affectedTests;
  @Singular
    List<String> recommendedActions;
    String llmInsight;
    boolean baselineMissing;
}
