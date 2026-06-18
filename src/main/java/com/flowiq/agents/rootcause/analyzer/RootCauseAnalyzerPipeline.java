package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.FailedTestContext;
import com.flowiq.agents.rootcause.model.RootCauseCategory;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import com.flowiq.agents.rootcause.model.RootCauseHypothesis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RootCauseAnalyzerPipeline {

    private static final Pattern TEST_BUG_PATTERN = Pattern.compile(
            "(?i)(wrong mock setup|unnecessary stubbing|test configuration error"
                    + "|at com\\.flowiq\\..*Test\\.|at org\\.testng\\.Assert"
                    + "|intentionally failing|TODO.*fix test)",
            Pattern.DOTALL);

    private static final Pattern ENVIRONMENT_PATTERN = Pattern.compile(
            "(?i)(applicationcontext failed|port already in use|docker|kubernetes"
                    + "|env variable|environment variable|failed to bind|bean creation exception"
                    + "|service not running|localhost:\\d+.*refused)",
            Pattern.DOTALL);

    private final List<RootCauseAnalyzer> analyzers = List.of(
            new BackendFailureAnalyzer(),
            new UiFailureAnalyzer(),
            new NetworkFailureAnalyzer(),
            new DatabaseFailureAnalyzer(),
            new AuthFailureAnalyzer(),
            new DataIssueAnalyzer()
    );

    public RootCauseFinding analyze(FailedTestContext context) {
        FailureAnalysisContext analysisContext = FailureAnalysisContext.from(context);
        List<RootCauseHypothesis> hypotheses = new ArrayList<>();

        for (RootCauseAnalyzer analyzer : analyzers) {
            analyzer.analyze(analysisContext).ifPresent(hypotheses::add);
        }
        detectTestBug(analysisContext).ifPresent(hypotheses::add);
        detectEnvironment(analysisContext).ifPresent(hypotheses::add);

        hypotheses.sort(Comparator.comparingInt(RootCauseHypothesis::getConfidence).reversed());
        RootCauseHypothesis primary = hypotheses.isEmpty()
                ? unknownHypothesis(analysisContext)
                : applyTieBreakers(hypotheses, analysisContext);

        return RootCauseFinding.builder()
                .failedTest(context.getExecution().getTestKey())
                .symptoms(buildSymptoms(context))
                .mostProbableRootCause(primary.getCategory())
                .confidence(primary.getConfidence())
                .evidence(primary.getEvidence())
                .recommendedFix(recommendFix(primary))
                .build();
    }

    private static RootCauseHypothesis applyTieBreakers(List<RootCauseHypothesis> hypotheses,
                                                      FailureAnalysisContext context) {
        RootCauseHypothesis top = hypotheses.get(0);
        String className = context.failedTest().getExecution().getClassName();
        boolean uiTest = className != null
                && (className.contains(".ui.") || className.contains("UiSmoke") || className.contains("E2E"));

        Optional<RootCauseHypothesis> uiMatch = hypotheses.stream()
                .filter(h -> h.getCategory() == RootCauseCategory.UI_BUG)
                .findFirst();
        if (uiTest && uiMatch.isPresent() && uiMatch.get().getConfidence() >= top.getConfidence() - 5) {
            return uiMatch.get();
        }

        Optional<RootCauseHypothesis> authMatch = hypotheses.stream()
                .filter(h -> h.getCategory() == RootCauseCategory.AUTH)
                .findFirst();
        if (context.combinedFailureText().matches("(?is).*(401|403|unauthorized).*")
                && authMatch.isPresent()) {
            return authMatch.get();
        }
        return top;
    }

    private static Optional<RootCauseHypothesis> detectTestBug(FailureAnalysisContext context) {
        String text = context.combinedFailureText();
        if (!TEST_BUG_PATTERN.matcher(text).find()) {
            return Optional.empty();
        }
        return Optional.of(RootCauseHypothesis.builder()
                .category(RootCauseCategory.TEST_BUG)
                .description("Failure originates from incorrect test setup, assertion, or test harness configuration.")
                .confidence(75)
                .evidenceItem("Stack trace or message points to test code / mock misuse")
                .build());
    }

    private static Optional<RootCauseHypothesis> detectEnvironment(FailureAnalysisContext context) {
        String text = context.combinedFailureText();
        if (!ENVIRONMENT_PATTERN.matcher(text).find()) {
            return Optional.empty();
        }
        List<String> evidence = new ArrayList<>();
        evidence.add("Environment or infrastructure startup failure detected");
        if (!context.backendLogExcerpt().isEmpty()) {
            evidence.add("Backend log: " + context.backendLogExcerpt().get(0));
        }
        return Optional.of(RootCauseHypothesis.builder()
                .category(RootCauseCategory.ENVIRONMENT)
                .description("Test environment misconfiguration, missing service, or infrastructure not ready.")
                .confidence(84)
                .evidence(evidence)
                .build());
    }

    private static RootCauseHypothesis unknownHypothesis(FailureAnalysisContext context) {
        List<String> evidence = new ArrayList<>();
        evidence.add("Insufficient correlated signals across failure text and artifacts");
        if (!context.failedTest().getTraces().isEmpty()) {
            evidence.add("Review Playwright trace: " + context.failedTest().getTraces().get(0));
        }
        return RootCauseHypothesis.builder()
                .category(RootCauseCategory.TEST_BUG)
                .description("Inconclusive — collect trace, video, and backend logs for the next failure.")
                .confidence(35)
                .evidence(evidence)
                .build();
    }

    private static String buildSymptoms(FailedTestContext context) {
        var record = context.getExecution();
        String message = record.getMessage();
        if (message == null || message.isBlank()) {
            message = "No failure message captured";
        }
        String suite = record.getSuite() == null ? "unknown" : record.getSuite();
        return suite + " suite failure: " + truncate(message, 240);
    }

    public String recommendFix(RootCauseHypothesis hypothesis) {
        return switch (hypothesis.getCategory()) {
            case BACKEND_BUG -> "Inspect backend logs and fix server exception or 5xx response; add API contract "
                    + "assertion for error payload if regression test.";
            case UI_BUG -> "Stabilize locator (prefer data-testid), add explicit wait, review screenshot/trace; "
                    + "fix page object if UI changed.";
            case TEST_BUG -> "Fix test setup, mocks, or assertions; verify test data seeding and expected values.";
            case NETWORK -> "Verify service health before suite; check CI networking, DNS, TLS certs; add retry "
                    + "for transient connectivity.";
            case AUTH -> "Refresh test credentials/token fixture; verify role mapping and auth header in test client.";
            case DATA -> "Reset or isolate test data; update fixture; fix DB seed/migration order; align expected "
                    + "values with API schema.";
            case ENVIRONMENT -> "Validate env vars, Docker/K8s services, and port bindings; ensure dependencies "
                    + "start before test execution.";
        };
    }

    private static String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }
}
