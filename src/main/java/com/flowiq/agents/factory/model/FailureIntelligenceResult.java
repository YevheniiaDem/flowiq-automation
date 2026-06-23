package com.flowiq.agents.factory.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class FailureIntelligenceResult {
    Instant analyzedAt;
    int failureIntelligenceScore;
    int flakinessMetric;
    int rootCauseConfidenceMetric;
    int locatorRecoveryPotentialMetric;
    int agentsSucceeded;
    int agentsFailed;
    @Singular("agentRun")
    List<FactoryAgentRunResult> agentRuns;
    @Singular("summaryLine")
    List<String> executiveSummary;
    @Singular("riskLine")
    List<String> topRisks;
    @Singular("actionLine")
    List<String> recommendedActions;
}
