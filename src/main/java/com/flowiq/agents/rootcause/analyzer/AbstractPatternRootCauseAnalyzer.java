package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;
import com.flowiq.agents.rootcause.model.RootCauseHypothesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPatternRootCauseAnalyzer implements RootCauseAnalyzer {

    private final RootCauseCategory category;
    private final Pattern pattern;
    private final String description;
    private final int baseConfidence;

    protected AbstractPatternRootCauseAnalyzer(RootCauseCategory category,
                                               Pattern pattern,
                                               String description,
                                               int baseConfidence) {
        this.category = category;
        this.pattern = pattern;
        this.description = description;
        this.baseConfidence = baseConfidence;
    }

    @Override
    public RootCauseCategory type() {
        return category;
    }

    @Override
    public Optional<RootCauseHypothesis> analyze(FailureAnalysisContext context) {
        String text = context.combinedFailureText();
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        List<String> evidence = new ArrayList<>();
        evidence.add("Matched pattern: " + pattern.pattern());
        String snippet = extractSnippet(text, matcher.start());
        if (!snippet.isBlank()) {
            evidence.add("Snippet: " + snippet);
        }
        appendArtifactEvidence(context, evidence);
        int confidence = adjustConfidence(baseConfidence, context);
        return Optional.of(RootCauseHypothesis.builder()
                .category(category)
                .description(description)
                .confidence(Math.min(100, confidence))
                .evidence(evidence)
                .build());
    }

    protected int adjustConfidence(int confidence, FailureAnalysisContext context) {
        return confidence;
    }

    protected void appendArtifactEvidence(FailureAnalysisContext context, List<String> evidence) {
        if (!context.failedTest().getScreenshots().isEmpty()) {
            evidence.add("Screenshot(s) available: " + context.failedTest().getScreenshots().size());
        }
        if (!context.failedTest().getTraces().isEmpty()) {
            evidence.add("Playwright trace(s) available: " + context.failedTest().getTraces().size());
        }
        if (!context.backendLogExcerpt().isEmpty()) {
            evidence.add("Correlated backend log line(s): " + context.backendLogExcerpt().size());
        }
    }

    private static String extractSnippet(String text, int start) {
        int from = Math.max(0, start - 40);
        int to = Math.min(text.length(), start + 120);
        return text.substring(from, to).replaceAll("\\s+", " ").trim();
    }
}
