package com.flowiq.agents.factory.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class AiQualityFactoryResult {
    Instant analyzedAt;
    int overallScore;
    FactoryCategory category;
    PrIntelligenceResult prIntelligence;
    GovernanceIntelligenceResult governanceIntelligence;
    FailureIntelligenceResult failureIntelligence;
    FactoryAgentRunResult releaseRiskRun;
    FactoryAgentRunResult smartTestGeneratorRun;
    @Singular("dimension")
    List<FactoryDimensionSummary> dimensions;
    @Singular("summaryLine")
    List<String> executiveSummary;
    @Singular("riskLine")
    List<String> topRisks;
    @Singular("actionLine")
    List<String> recommendedActions;
}
