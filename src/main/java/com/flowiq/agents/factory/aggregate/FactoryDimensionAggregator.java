package com.flowiq.agents.factory.aggregate;

import com.flowiq.agents.factory.model.FactoryAgentRunResult;
import com.flowiq.agents.factory.model.FactoryDimensionSummary;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import com.flowiq.agents.factory.model.GovernanceIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceResult;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.ReleaseRiskCategory;

import java.util.ArrayList;
import java.util.List;

public class FactoryDimensionAggregator {

    public List<FactoryDimensionSummary> aggregate(
            PrIntelligenceResult pr,
            GovernanceIntelligenceResult governance,
            FailureIntelligenceResult failure,
            FactoryAgentRunResult releaseRun,
            FactoryAgentRunResult generatorRun) {
        return List.of(
                prHealth(pr),
                governanceHealth(governance),
                failureIntelligence(failure),
                releaseReadiness(releaseRun),
                testIntelligence(generatorRun));
    }

    private FactoryDimensionSummary prHealth(PrIntelligenceResult pr) {
        List<String> highlights = new ArrayList<>();
        if (pr != null) {
            highlights.add("PR Quality Score: " + pr.getPrQualityScore() + "/100");
            highlights.add("Verdict: " + pr.getVerdict().name().replace('_', ' '));
            highlights.add("Agents succeeded: " + pr.getAgentsSucceeded());
        }
        return FactoryDimensionSummary.builder()
                .name("PR Health")
                .healthScore(pr != null ? pr.getPrQualityScore() : 50)
                .contributingComponent("PrIntelligenceOrchestrator")
                .highlights(highlights)
                .build();
    }

    private FactoryDimensionSummary governanceHealth(GovernanceIntelligenceResult governance) {
        List<String> highlights = new ArrayList<>();
        int score = 50;
        if (governance != null) {
            score = governance.getGovernanceHealthScore();
            highlights.add("Governance Health Score: " + score + "/100");
            highlights.add("Category: " + governance.getCategory().name());
        }
        return FactoryDimensionSummary.builder()
                .name("Governance Health")
                .healthScore(score)
                .contributingComponent("GovernanceIntelligenceOrchestrator")
                .highlights(highlights)
                .build();
    }

    private FactoryDimensionSummary failureIntelligence(FailureIntelligenceResult failure) {
        List<String> highlights = new ArrayList<>();
        int score = 50;
        if (failure != null) {
            score = failure.getFailureIntelligenceScore();
            highlights.add("Failure Intelligence Score: " + score + "/100");
            highlights.add("Flakiness metric: " + failure.getFlakinessMetric() + "/100");
            highlights.add("Root cause confidence: " + failure.getRootCauseConfidenceMetric() + "/100");
            highlights.add("Locator recovery potential: " + failure.getLocatorRecoveryPotentialMetric() + "/100");
        }
        return FactoryDimensionSummary.builder()
                .name("Failure Intelligence")
                .healthScore(score)
                .contributingComponent("FailureIntelligenceOrchestrator")
                .highlights(highlights)
                .build();
    }

    private FactoryDimensionSummary releaseReadiness(FactoryAgentRunResult releaseRun) {
        List<String> highlights = new ArrayList<>();
        int score = 50;
        if (releaseRun != null && releaseRun.isSuccess()) {
            ReleaseRiskAssessmentResult result = releaseRun.payloadAs(ReleaseRiskAssessmentResult.class);
            if (result != null) {
                score = (int) Math.round(100.0 - result.getReleaseRiskScore());
                highlights.add("Release risk score: " + result.getReleaseRiskScore());
                highlights.add("Category: " + result.getRiskCategory());
                highlights.add("Recommendation: " + result.getRecommendation());
                if (result.getRiskCategory() == ReleaseRiskCategory.RED) {
                    score = Math.min(score, 40);
                }
            }
        } else {
            highlights.add("Release risk assessment unavailable");
        }
        return FactoryDimensionSummary.builder()
                .name("Release Readiness")
                .healthScore(score)
                .contributingComponent("ReleaseRiskAssessmentAgent")
                .highlights(highlights)
                .build();
    }

    private FactoryDimensionSummary testIntelligence(FactoryAgentRunResult generatorRun) {
        List<String> highlights = new ArrayList<>();
        int score = 75;
        if (generatorRun != null && generatorRun.isSuccess()) {
            ScenarioGenerationResult result = generatorRun.payloadAs(ScenarioGenerationResult.class);
            if (result != null) {
                highlights.add("Endpoints analyzed: " + result.getEndpointsAnalyzed());
                highlights.add("Scenario candidates: " + result.getScenarios().size());
                score = Math.max(40, 100 - Math.min(40, result.getScenarios().size()));
            }
        } else {
            highlights.add("Smart test generator unavailable");
            score = 50;
        }
        return FactoryDimensionSummary.builder()
                .name("Test Intelligence")
                .healthScore(score)
                .contributingComponent("SmartTestGeneratorAgent")
                .highlights(highlights)
                .build();
    }
}
