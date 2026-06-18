package com.flowiq.agents.traceability.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TraceabilityAnalysisResult {
    Instant analyzedAt;
    double overallCoveragePercent;
    int featureCount;
    int documentedFeatureCount;
    int openApiEndpointCount;
  @Singular("matrixRow")
    List<FeatureTraceabilityRow> matrix;
  @Singular("issue")
    List<TraceabilityIssue> issues;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
