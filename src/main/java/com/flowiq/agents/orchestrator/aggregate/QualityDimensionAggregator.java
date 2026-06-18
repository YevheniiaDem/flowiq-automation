package com.flowiq.agents.orchestrator.aggregate;

import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.orchestrator.model.QualityAgentResultsBundle;
import com.flowiq.agents.orchestrator.model.QualityAgentType;
import com.flowiq.agents.orchestrator.model.QualityDimensionSummary;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;

import java.util.ArrayList;
import java.util.List;

public class QualityDimensionAggregator {

    public List<QualityDimensionSummary> aggregate(QualityAgentResultsBundle bundle) {
        return List.of(
                apiHealth(bundle),
                coverageHealth(bundle),
                releaseRisk(bundle),
                architectureHealth(bundle),
                flakyStatus(bundle),
                regressionRisk(bundle),
                traceabilityStatus(bundle));
    }

    private QualityDimensionSummary apiHealth(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 100;

        var apiChange = bundle.payload(QualityAgentType.API_CHANGE_DETECTION, AnalysisResult.class);
        if (apiChange.isPresent()) {
            AnalysisResult result = apiChange.get();
            highlights.add("API changes detected: " + result.getChanges().size());
            highlights.add("Risk level: " + result.getRiskLevel());
            score -= switch (result.getRiskLevel()) {
                case HIGH -> 35;
                case MEDIUM -> 20;
                case LOW -> result.getChanges().isEmpty() ? 0 : 5;
            };
            if (result.getChanges().stream().anyMatch(c -> c.isBreaking())) {
                score -= 15;
                highlights.add("Breaking API changes present");
            }
        } else {
            score = 50;
            highlights.add("API change detection did not complete");
        }

        var testReview = bundle.payload(QualityAgentType.TEST_REVIEW, TestReviewResult.class);
        if (testReview.isPresent()) {
            TestReviewResult result = testReview.get();
            highlights.add("PR test review verdict: " + result.getOverallVerdict());
            score -= switch (result.getOverallVerdict()) {
                case REJECTED -> 25;
                case APPROVED_WITH_RISK -> 10;
                case APPROVED -> 0;
            };
            if (result.getRejectedCount() > 0) {
                highlights.add("Rejected features: " + result.getRejectedCount());
            }
        }

        return dimension("API Health", score, highlights,
                "ApiChangeDetectionAgent", "TestReviewAgent");
    }

    private QualityDimensionSummary coverageHealth(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 70;

        var gap = bundle.payload(QualityAgentType.TEST_GAP_ANALYZER, TestGapAnalysisResult.class);
        if (gap.isPresent()) {
            TestGapAnalysisResult result = gap.get();
            score = (int) Math.round(result.getOverallCoveragePercent());
            highlights.add(String.format("Overall test coverage: %.1f%%", result.getOverallCoveragePercent()));
            highlights.add("Coverage gaps: " + result.getGaps().size());
            long criticalGaps = result.getGaps().stream()
                    .filter(g -> g.getSeverity() == GapSeverity.CRITICAL || g.getSeverity() == GapSeverity.HIGH)
                    .count();
            if (criticalGaps > 0) {
                score = Math.max(0, score - (int) criticalGaps * 5);
                highlights.add("High/critical gaps: " + criticalGaps);
            }
        } else {
            highlights.add("Test gap analysis unavailable");
        }

        var generator = bundle.payload(QualityAgentType.SMART_TEST_GENERATOR, ScenarioGenerationResult.class);
        if (generator.isPresent()) {
            ScenarioGenerationResult result = generator.get();
            highlights.add("Generated scenario candidates: " + result.getScenarios().size());
            if (result.getScenarios().size() > 15) {
                score = Math.max(0, score - 5);
            }
        }

        return dimension("Coverage Health", clamp(score), highlights,
                "TestGapAnalyzerAgent", "SmartTestGeneratorAgent");
    }

    private QualityDimensionSummary releaseRisk(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 75;

        var release = bundle.payload(QualityAgentType.RELEASE_RISK_ASSESSMENT, ReleaseRiskAssessmentResult.class);
        if (release.isPresent()) {
            ReleaseRiskAssessmentResult result = release.get();
            score = (int) Math.round(100.0 - result.getReleaseRiskScore());
            highlights.add(String.format("Release risk score: %.1f", result.getReleaseRiskScore()));
            highlights.add("Category: " + result.getRiskCategory());
            highlights.add("Recommendation: " + result.getRecommendation());
            if (result.getRiskCategory() == ReleaseRiskCategory.RED) {
                score = Math.min(score, 40);
            } else if (result.getRiskCategory() == ReleaseRiskCategory.YELLOW) {
                score = Math.min(score, 65);
            }
            if (!result.getCriticalFailures().isEmpty()) {
                highlights.add("Critical failures: " + result.getCriticalFailures().size());
            }
        } else {
            highlights.add("Release risk assessment unavailable");
            score = 50;
        }

        return dimension("Release Risk", clamp(score), highlights, "ReleaseRiskAssessmentAgent");
    }

    private QualityDimensionSummary architectureHealth(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 80;

        var drift = bundle.payload(QualityAgentType.ARCHITECTURE_DRIFT, ArchitectureDriftResult.class);
        if (drift.isPresent()) {
            ArchitectureDriftResult result = drift.get();
            score = result.getArchitectureHealthScore();
            highlights.add("Architecture health: " + result.getArchitectureHealthScore() + "/100");
            highlights.add("Drift issues: " + result.getIssuesFound());
            if (result.getCriticalIssues() > 0) {
                highlights.add("Critical drift issues: " + result.getCriticalIssues());
            }
        } else {
            highlights.add("Architecture drift analysis unavailable");
            score = 50;
        }

        return dimension("Architecture Health", clamp(score), highlights, "ArchitectureDriftAgent");
    }

    private QualityDimensionSummary flakyStatus(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 85;

        var flaky = bundle.payload(QualityAgentType.FLAKY_TEST_INVESTIGATOR, FlakyInvestigationResult.class);
        if (flaky.isPresent()) {
            FlakyInvestigationResult result = flaky.get();
            score = (int) Math.round(100.0 - result.getPortfolioFlakinessPercent());
            highlights.add(String.format("Portfolio flakiness: %.1f%%", result.getPortfolioFlakinessPercent()));
            highlights.add("Flaky tests: " + result.getFlakyTestCount());
            highlights.add(String.format("Pass rate: %.1f%%", result.getPortfolioPassRate()));
            score = Math.max(0, score - result.getFlakyTestCount() * 2);
        } else {
            highlights.add("Flaky test investigation unavailable");
            score = 60;
        }

        var healing = bundle.payload(QualityAgentType.SELF_HEALING_LOCATOR, SelfHealingResult.class);
        if (healing.isPresent()) {
            SelfHealingResult result = healing.get();
            highlights.add("Locator healing suggestions: " + result.getSuggestionsGenerated());
            score = Math.max(0, score - result.getSuggestionsGenerated() * 3);
        }

        var rootCause = bundle.payload(QualityAgentType.ROOT_CAUSE_ANALYSIS, RootCauseAnalysisResult.class);
        if (rootCause.isPresent()) {
            RootCauseAnalysisResult result = rootCause.get();
            highlights.add("Failures analyzed (root cause): " + result.getFailuresAnalyzed());
            highlights.add("High-confidence findings: " + result.getHighConfidenceFindings());
            score = Math.max(0, score - result.getHighConfidenceFindings() * 4);
        }

        return dimension("Flaky Status", clamp(score), highlights,
                "FlakyTestInvestigator", "SelfHealingLocatorAgent", "RootCauseAnalysisAgent");
    }

    private QualityDimensionSummary regressionRisk(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 80;

        var regression = bundle.payload(QualityAgentType.RISK_BASED_REGRESSION, RiskBasedRegressionResult.class);
        if (regression.isPresent()) {
            RiskBasedRegressionResult result = regression.get();
            score = switch (result.getRecommendation()) {
                case SMOKE_ONLY -> 95;
                case PARTIAL_REGRESSION -> 78;
                case FULL_REGRESSION -> 55;
            };
            highlights.add("Regression scope: " + result.getRecommendation().name().replace('_', ' '));
            highlights.add("Modules analyzed: " + result.getModulesAnalyzed());
            highlights.add("Selected test classes: " + result.getTotalSelectedTestClasses());
            highlights.add("Estimated runtime: " + result.getEstimatedTotalExecutionMinutes() + " min");
        } else {
            highlights.add("Risk-based regression planning unavailable");
            score = 50;
        }

        return dimension("Regression Risk", clamp(score), highlights, "RiskBasedRegressionAgent");
    }

    private QualityDimensionSummary traceabilityStatus(QualityAgentResultsBundle bundle) {
        List<String> highlights = new ArrayList<>();
        int score = 75;

        var traceability = bundle.payload(QualityAgentType.REQUIREMENTS_TRACEABILITY, TraceabilityAnalysisResult.class);
        if (traceability.isPresent()) {
            TraceabilityAnalysisResult result = traceability.get();
            score = (int) Math.round(result.getOverallCoveragePercent());
            highlights.add(String.format("Traceability coverage: %.1f%%", result.getOverallCoveragePercent()));
            highlights.add("Features documented: " + result.getDocumentedFeatureCount()
                    + "/" + result.getFeatureCount());
            highlights.add("OpenAPI endpoints: " + result.getOpenApiEndpointCount());
            highlights.add("Traceability issues: " + result.getIssues().size());
            score = Math.max(0, score - result.getIssues().size() * 3);
        } else {
            highlights.add("Requirements traceability analysis unavailable");
            score = 50;
        }

        return dimension("Traceability Status", clamp(score), highlights, "RequirementsTraceabilityAgent");
    }

    private static QualityDimensionSummary dimension(String name,
                                                     int score,
                                                     List<String> highlights,
                                                     String... agents) {
        var builder = QualityDimensionSummary.builder()
                .name(name)
                .healthScore(clamp(score));
        highlights.forEach(builder::highlight);
        for (String agent : agents) {
            builder.contributingAgent(agent);
        }
        return builder.build();
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
