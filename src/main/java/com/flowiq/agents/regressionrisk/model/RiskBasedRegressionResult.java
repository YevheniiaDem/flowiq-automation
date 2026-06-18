package com.flowiq.agents.regressionrisk.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class RiskBasedRegressionResult {
    Instant analyzedAt;
    RegressionScopeRecommendation recommendation;
    int modulesAnalyzed;
    int totalSelectedTestClasses;
    int estimatedTotalExecutionMinutes;
  @Singular("modulePlan")
    List<ModuleChangeImpact> modulePlans;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
