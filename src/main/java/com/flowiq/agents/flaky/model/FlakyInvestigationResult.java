package com.flowiq.agents.flaky.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class FlakyInvestigationResult {
    Instant analyzedAt;
    int totalExecutionsAnalyzed;
    int uniqueTests;
    int flakyTestCount;
    double portfolioPassRate;
    double portfolioFailureRate;
    double portfolioFlakinessPercent;
  @Singular
    List<FlakyTestFinding> topUnstableTests;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
