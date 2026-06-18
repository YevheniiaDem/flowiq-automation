package com.flowiq.agents.release.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class ReleaseRiskAssessmentResult {
    Instant assessedAt;
    double releaseRiskScore;
    ReleaseRiskCategory riskCategory;
    ReleaseRecommendation recommendation;
    SuiteExecutionSummary regressionSummary;
    SuiteExecutionSummary smokeSummary;
    SuiteExecutionSummary contractSummary;
    FlakyReportInsight flakyInsight;
    ApiChangeReportInsight apiChangeInsight;
  @Singular
    List<CriticalFailure> criticalFailures;
  @Singular
    List<BlockedArea> blockedAreas;
  @Singular
    List<String> recommendedActions;
  @Singular("summaryLine")
    List<String> executiveSummary;
    Map<String, Double> scoreBreakdown;
    String dataSourcesSummary;
}
