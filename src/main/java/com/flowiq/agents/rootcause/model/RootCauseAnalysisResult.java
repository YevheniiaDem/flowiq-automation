package com.flowiq.agents.rootcause.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class RootCauseAnalysisResult {
    Instant analyzedAt;
    int failuresAnalyzed;
    int highConfidenceFindings;
  @Singular("finding")
    List<RootCauseFinding> findings;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
