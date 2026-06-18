package com.flowiq.agents.flaky.prioritizer;

import com.flowiq.agents.flaky.model.FlakyTestFinding;
import com.flowiq.agents.flaky.model.TestStabilityMetrics;

public class FlakyTestPriorityScorer {

    public double score(TestStabilityMetrics metrics, int ciFailureCount, String suite) {
        double flakinessWeight = metrics.getFlakinessPercent() * 0.40;
        double failureWeight = metrics.getFailureRate() * 0.30;
        double suiteWeight = suiteWeight(suite) * 0.20;
        double ciWeight = Math.min(ciFailureCount * 5.0, 20.0) * 0.10;
        return Math.min(100.0, flakinessWeight + failureWeight + suiteWeight + ciWeight);
    }

    public FlakyTestFinding withScore(FlakyTestFinding finding) {
        double score = score(finding.getMetrics(), finding.getCiFailureCount(), finding.getMetrics().getSuite());
        return FlakyTestFinding.builder()
                .metrics(finding.getMetrics())
                .primaryRootCause(finding.getPrimaryRootCause())
                .alternateCauses(finding.getAlternateCauses())
                .recommendedFix(finding.getRecommendedFix())
                .priorityScore(score)
                .ciFailureCount(finding.getCiFailureCount())
                .lastFailureMessage(finding.getLastFailureMessage())
                .build();
    }

    private double suiteWeight(String suite) {
        return switch (suite == null ? "other" : suite) {
            case "smoke", "ui" -> 25.0;
            case "contract" -> 22.0;
            case "regression" -> 18.0;
            case "integration" -> 15.0;
            default -> 10.0;
        };
    }
}
