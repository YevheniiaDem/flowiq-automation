package com.flowiq.agents.gap.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TestGapAnalysisResult {
    Instant analyzedAt;
    double overallCoveragePercent;
  @Singular
    List<ModuleCoverage> modules;
  @Singular
    List<TestGap> gaps;
  @Singular
    List<String> recommendedTests;
    String llmInsight;
}
