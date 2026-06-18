package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseHypothesis;
import com.flowiq.agents.flaky.model.RootCauseType;
import com.flowiq.agents.flaky.model.TestExecutionRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RootCauseAnalyzerPipeline {

    private final List<RootCauseAnalyzer> analyzers = List.of(
            new TimeoutRootCauseAnalyzer(),
            new LocatorRootCauseAnalyzer(),
            new NetworkRootCauseAnalyzer(),
            new BackendInstabilityRootCauseAnalyzer(),
            new RaceConditionRootCauseAnalyzer()
    );

    public RootCauseAnalysis analyze(List<TestExecutionRecord> failureRuns) {
        String combined = failureRuns.stream()
                .map(r -> safe(r.getMessage()) + "\n" + safe(r.getStackTrace()))
                .reduce("", (a, b) -> a + "\n" + b);

        List<RootCauseHypothesis> hypotheses = new ArrayList<>();
        for (RootCauseAnalyzer analyzer : analyzers) {
            RootCauseHypothesis hypothesis = analyzer.analyze(combined, failureRuns);
            if (hypothesis != null) {
                hypotheses.add(hypothesis);
            }
        }
        hypotheses.sort(Comparator.comparing(RootCauseHypothesis::getConfidence).reversed());

        RootCauseHypothesis primary;
        if (combined.contains("TimeoutError") || combined.matches("(?is).*\\btimeout\\b.*exceeded.*")) {
            primary = hypotheses.stream()
                    .filter(h -> h.getType() == RootCauseType.TIMEOUT)
                    .findFirst()
                    .orElse(hypotheses.isEmpty() ? unknown() : hypotheses.get(0));
        } else {
            primary = hypotheses.isEmpty() ? unknown() : hypotheses.get(0);
        }

        return new RootCauseAnalysis(primary,
                hypotheses.size() > 1 ? hypotheses.subList(1, hypotheses.size()) : List.of());
    }

    public String recommendFix(RootCauseHypothesis primary, String suite) {
        return switch (primary.getType()) {
            case TIMEOUT -> "Increase explicit waits; use Playwright auto-waiting locators; raise timeout for "
                    + suite + " suite or mock slow dependencies.";
            case LOCATOR_ISSUE -> "Replace CSS/text selectors with data-testid; add waitForVisible before interactions; "
                    + "review strict-mode violations.";
            case NETWORK_INSTABILITY -> "Add health-check gate before suite; enable RetrySupport for transient errors; "
                    + "verify CI service connectivity.";
            case BACKEND_INSTABILITY -> "Stabilize test data seeding; add API retry with backoff; investigate 5xx in "
                    + "backend logs correlated with test timestamp.";
            case RACE_CONDITION -> "Add explicit state assertions before actions; isolate test data; avoid parallel "
                    + "execution on shared resources.";
            case UNKNOWN -> "Quarantine test with @Test(retryAnalyzer = ...) for data collection; attach trace/video "
                    + "and review last 5 CI failures.";
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static RootCauseHypothesis unknown() {
        return RootCauseHypothesis.builder()
                .type(RootCauseType.UNKNOWN)
                .description("Insufficient failure signal — collect Playwright traces and backend logs for next failure.")
                .confidence(0.3)
                .build();
    }

    public record RootCauseAnalysis(RootCauseHypothesis primary, List<RootCauseHypothesis> alternates) {
    }
}
