package com.flowiq.agents.factory.scorer;

import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.factory.model.FactoryAgentResultsBundle;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.GovernanceCategory;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.maintenance.model.TestMaintenanceResult;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;

import java.util.ArrayList;
import java.util.List;

public class GovernanceIntelligenceScorer {

    public int calculateScore(FactoryAgentResultsBundle bundle) {
        List<Integer> components = new ArrayList<>();

        bundle.payload(FactoryAgentType.ARCHITECTURE_DRIFT, ArchitectureDriftResult.class)
                .ifPresentOrElse(r -> components.add(r.getArchitectureHealthScore()),
                        () -> components.add(50));

        bundle.payload(FactoryAgentType.REQUIREMENTS_TRACEABILITY, TraceabilityAnalysisResult.class)
                .ifPresentOrElse(r -> components.add((int) Math.round(r.getOverallCoveragePercent())),
                        () -> components.add(50));

        bundle.payload(FactoryAgentType.TEST_GAP_ANALYZER, TestGapAnalysisResult.class)
                .ifPresentOrElse(r -> {
                    int gapScore = (int) Math.round(r.getOverallCoveragePercent());
                    long criticalGaps = r.getGaps().stream()
                            .filter(g -> g.getSeverity() == GapSeverity.CRITICAL || g.getSeverity() == GapSeverity.HIGH)
                            .count();
                    components.add(Math.max(0, gapScore - (int) criticalGaps * 4));
                }, () -> components.add(50));

        bundle.payload(FactoryAgentType.TEST_MAINTENANCE, TestMaintenanceResult.class)
                .ifPresentOrElse(r -> components.add(r.getAutomationHealthScore()),
                        () -> components.add(50));

        if (components.isEmpty()) {
            return 0;
        }
        return clamp((int) Math.round(components.stream().mapToInt(Integer::intValue).average().orElse(0)));
    }

    public GovernanceCategory categorize(int score) {
        return GovernanceCategory.fromScore(score);
    }

    public List<String> topRisks(FactoryAgentResultsBundle bundle) {
        List<String> risks = new ArrayList<>();
        bundle.payload(FactoryAgentType.ARCHITECTURE_DRIFT, ArchitectureDriftResult.class)
                .ifPresent(r -> {
                    if (r.getCriticalIssues() > 0) {
                        risks.add("Architecture drift: " + r.getCriticalIssues() + " critical issue(s)");
                    }
                });
        bundle.payload(FactoryAgentType.TEST_GAP_ANALYZER, TestGapAnalysisResult.class)
                .ifPresent(r -> {
                    long critical = r.getGaps().stream()
                            .filter(g -> g.getSeverity() == GapSeverity.CRITICAL)
                            .count();
                    if (critical > 0) {
                        risks.add("Test gaps: " + critical + " critical coverage gap(s)");
                    }
                });
        bundle.payload(FactoryAgentType.TEST_MAINTENANCE, TestMaintenanceResult.class)
                .ifPresent(r -> {
                    if (r.getAutomationHealthScore() < 60) {
                        risks.add("Automation framework health below threshold (" + r.getAutomationHealthScore() + "/100)");
                    }
                });
        bundle.payload(FactoryAgentType.REQUIREMENTS_TRACEABILITY, TraceabilityAnalysisResult.class)
                .ifPresent(r -> {
                    if (r.getOverallCoveragePercent() < 60) {
                        risks.add("Low requirements traceability coverage (" + String.format("%.1f", r.getOverallCoveragePercent()) + "%)");
                    }
                });
        return risks;
    }

    public List<String> recommendedActions(FactoryAgentResultsBundle bundle, GovernanceCategory category) {
        List<String> actions = new ArrayList<>();
        if (category == GovernanceCategory.CRITICAL || category == GovernanceCategory.WARNING) {
            actions.add("Prioritize architecture drift and coverage gap remediation");
        }
        bundle.payload(FactoryAgentType.TEST_MAINTENANCE, TestMaintenanceResult.class)
                .ifPresent(r -> r.getTopPriorityFixes().stream().limit(2).forEach(actions::add));
        if (actions.isEmpty()) {
            actions.add("Governance posture is stable — continue monitoring drift trends");
        }
        return actions;
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
