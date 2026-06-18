package com.flowiq.agents.orchestrator.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class QualityIntelligenceResult {
    Instant analyzedAt;
    int qualityScore;
    QualityCategory category;
  @Singular("dimension")
    List<QualityDimensionSummary> dimensions;
  @Singular("agentRun")
    List<QualityAgentRunResult> agentRuns;
  @Singular("summaryLine")
    List<String> executiveSummary;
    int agentsSucceeded;
    int agentsFailed;
}
