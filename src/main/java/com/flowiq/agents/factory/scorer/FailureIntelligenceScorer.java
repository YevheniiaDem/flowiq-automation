package com.flowiq.agents.factory.scorer;

import com.flowiq.agents.factory.model.FactoryAgentResultsBundle;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import com.flowiq.agents.selfhealing.model.LocatorConfidence;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;

import java.util.ArrayList;
import java.util.List;

public class FailureIntelligenceScorer {

    public FailureMetrics calculateMetrics(FactoryAgentResultsBundle bundle) {
        int flakiness = bundle.payload(FactoryAgentType.FLAKY_TEST_INVESTIGATOR, FlakyInvestigationResult.class)
                .map(r -> clamp((int) Math.round(100.0 - r.getPortfolioFlakinessPercent()) - r.getFlakyTestCount() * 2))
                .orElse(60);

        int rootCauseConfidence = bundle.payload(FactoryAgentType.ROOT_CAUSE_ANALYSIS, RootCauseAnalysisResult.class)
                .map(this::rootCauseConfidenceScore)
                .orElse(65);

        int locatorRecovery = bundle.payload(FactoryAgentType.SELF_HEALING_LOCATOR, SelfHealingResult.class)
                .map(this::locatorRecoveryScore)
                .orElse(70);

        return new FailureMetrics(
                clamp(flakiness),
                clamp(rootCauseConfidence),
                clamp(locatorRecovery));
    }

    public int calculateScore(FailureMetrics metrics) {
        return clamp((int) Math.round(
                metrics.flakinessMetric() * 0.40
                        + metrics.rootCauseConfidenceMetric() * 0.35
                        + metrics.locatorRecoveryPotentialMetric() * 0.25));
    }

    public List<String> topRisks(FactoryAgentResultsBundle bundle) {
        List<String> risks = new ArrayList<>();
        bundle.payload(FactoryAgentType.FLAKY_TEST_INVESTIGATOR, FlakyInvestigationResult.class)
                .ifPresent(r -> {
                    if (r.getFlakyTestCount() > 0) {
                        risks.add("Flaky tests detected: " + r.getFlakyTestCount());
                    }
                });
        bundle.payload(FactoryAgentType.ROOT_CAUSE_ANALYSIS, RootCauseAnalysisResult.class)
                .ifPresent(r -> {
                    if (r.getHighConfidenceFindings() > 0) {
                        risks.add("High-confidence failure root causes pending remediation: " + r.getHighConfidenceFindings());
                    }
                });
        bundle.payload(FactoryAgentType.SELF_HEALING_LOCATOR, SelfHealingResult.class)
                .ifPresent(r -> {
                    if (r.getSuggestionsGenerated() > 0) {
                        risks.add("UI locator failures with healing candidates: " + r.getSuggestionsGenerated());
                    }
                });
        return risks;
    }

    public List<String> recommendedActions(FactoryAgentResultsBundle bundle, FailureMetrics metrics) {
        List<String> actions = new ArrayList<>();
        if (metrics.flakinessMetric() < 70) {
            actions.add("Stabilize flaky tests identified by FlakyTestInvestigator");
        }
        if (metrics.rootCauseConfidenceMetric() < 70) {
            actions.add("Triage high-confidence root cause findings before next CI run");
        }
        if (metrics.locatorRecoveryPotentialMetric() < 70) {
            actions.add("Apply high-confidence self-healing locator suggestions to page objects");
        }
        if (actions.isEmpty()) {
            actions.add("Failure intelligence signals are within acceptable ranges");
        }
        return actions;
    }

    private int rootCauseConfidenceScore(RootCauseAnalysisResult result) {
        if (result.getFindings().isEmpty()) {
            return result.getFailuresAnalyzed() == 0 ? 90 : 55;
        }
        double avg = result.getFindings().stream()
                .mapToInt(RootCauseFinding::getConfidence)
                .average()
                .orElse(50);
        return (int) Math.round(avg);
    }

    private int locatorRecoveryScore(SelfHealingResult result) {
        if (result.getSuggestions().isEmpty()) {
            return result.getFailuresAnalyzed() == 0 ? 90 : 60;
        }
        long highConfidence = result.getSuggestions().stream()
                .filter(s -> s.getConfidence() == LocatorConfidence.HIGH)
                .count();
        int base = 50 + (int) highConfidence * 15;
        return Math.min(100, base + result.getSuggestionsGenerated() * 2);
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    public record FailureMetrics(
            int flakinessMetric,
            int rootCauseConfidenceMetric,
            int locatorRecoveryPotentialMetric) {
    }
}
