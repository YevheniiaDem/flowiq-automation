package com.flowiq.agents.selfhealing.generator;

import com.flowiq.agents.selfhealing.analyzer.LocatorFailureAnalyzer;
import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.engine.LocatorSimilarityEngine;
import com.flowiq.agents.selfhealing.llm.SelfHealingLlmProvider;
import com.flowiq.agents.selfhealing.model.DomElement;
import com.flowiq.agents.selfhealing.model.LocatorConfidence;
import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorRisk;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import com.flowiq.agents.selfhealing.model.LocatorType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SuggestedLocatorGenerator {

    private final SelfHealingAgentConfig config;
    private final LocatorFailureAnalyzer failureAnalyzer;
    private final SelfHealingLlmProvider llmProvider;

    public SuggestedLocatorGenerator(SelfHealingAgentConfig config,
                                     LocatorFailureAnalyzer failureAnalyzer,
                                     SelfHealingLlmProvider llmProvider) {
        this.config = config;
        this.failureAnalyzer = failureAnalyzer;
        this.llmProvider = llmProvider;
    }

    public Optional<LocatorSuggestion> generate(LocatorFailureContext context) {
        LocatorFailureContext enriched = failureAnalyzer.enrich(context);
        if (enriched.getDomElements() == null || enriched.getDomElements().isEmpty()) {
            return Optional.empty();
        }

        List<String> hints = failureAnalyzer.extractLocatorHints(enriched.getOldLocator());
        ScoredCandidate best = findBestCandidate(enriched.getDomElements(), hints);
        if (best == null) {
            return Optional.empty();
        }

        LocatorConfidence confidence = mapConfidence(Math.min(1.0, best.score()), best.type());
        LocatorRisk risk = mapRisk(best.type());
        String reasoning = buildReasoning(enriched, best);

        LocatorSuggestion suggestion = LocatorSuggestion.builder()
                .testKey(enriched.getTestKey())
                .testName(enriched.getTestName())
                .oldLocator(enriched.getOldLocator())
                .suggestedLocator(best.locatorExpression())
                .suggestedLocatorType(best.type())
                .confidence(confidence)
                .similarityScore(round(best.score()))
                .reasoning(reasoning)
                .risk(risk)
                .screenshotPath(pathToString(enriched.getScreenshotPath()))
                .domSnapshotPath(pathToString(enriched.getDomSnapshotPath()))
                .llmEnriched(false)
                .build();

        if (llmProvider.isConfigured()) {
            return llmProvider.enrichSuggestion(enriched, suggestion);
        }
        return Optional.of(suggestion);
    }

    private ScoredCandidate findBestCandidate(List<DomElement> elements, List<String> hints) {
        List<ScoredCandidate> candidates = new ArrayList<>();
        for (DomElement element : elements) {
            candidates.addAll(scoreElement(element, hints));
        }
        return candidates.stream()
                .max(Comparator.comparingDouble(ScoredCandidate::score)
                        .thenComparingInt(c -> typePriority(c.type())))
                .orElse(null);
    }

    private static int typePriority(LocatorType type) {
        return switch (type) {
            case TEST_ID -> 5;
            case ARIA_LABEL -> 4;
            case ROLE -> 3;
            case TEXT -> 2;
            case CSS, UNKNOWN -> 1;
        };
    }

    private List<ScoredCandidate> scoreElement(DomElement element, List<String> hints) {
        List<ScoredCandidate> scored = new ArrayList<>();
        if (element.getTestId() != null && !element.getTestId().isBlank()) {
            double score = Math.min(1.0, bestHintScore(element.getTestId(), hints) + 0.15);
            scored.add(new ScoredCandidate(
                    "page.getByTestId('" + element.getTestId() + "')",
                    LocatorType.TEST_ID, score, element));
        }
        if (element.getAriaLabel() != null && !element.getAriaLabel().isBlank()) {
            double score = bestHintScore(element.getAriaLabel(), hints);
            scored.add(new ScoredCandidate(
                    "page.getByLabel('" + escape(element.getAriaLabel()) + "')",
                    LocatorType.ARIA_LABEL, score, element));
        }
        String role = element.getRole() != null ? element.getRole() : defaultRole(element.getTagName());
        if (role != null && element.getTextContent() != null && !element.getTextContent().isBlank()
                && ("button".equals(element.getTagName()) || "a".equals(element.getTagName()))) {
            double score = Math.max(
                    bestHintScore(element.getTextContent(), hints),
                    bestHintScore(role, hints) * 0.8);
            scored.add(new ScoredCandidate(
                    "page.getByRole('" + role + "', { name: '" + escape(element.getTextContent()) + "' })",
                    LocatorType.ROLE, score, element));
        }
        if (element.getTextContent() != null && element.getTextContent().length() >= 2) {
            double score = bestHintScore(element.getTextContent(), hints) * 0.9;
            scored.add(new ScoredCandidate(
                    "page.getByText('" + escape(element.getTextContent()) + "')",
                    LocatorType.TEXT, score, element));
        }
        if (element.getId() != null && !element.getId().isBlank()) {
            double score = bestHintScore(element.getId(), hints) * 0.85;
            scored.add(new ScoredCandidate(
                    "page.locator('#" + element.getId() + "')",
                    LocatorType.CSS, score, element));
        }
        if (element.getCssClasses() != null && !element.getCssClasses().isBlank()) {
            String primaryClass = element.getCssClasses().split("\\s+")[0];
            double score = bestHintScore(primaryClass, hints) * 0.75;
            scored.add(new ScoredCandidate(
                    "page.locator('." + primaryClass + "')",
                    LocatorType.CSS, score, element));
        }
        return scored;
    }

    private static double bestHintScore(String candidateValue, List<String> hints) {
        if (hints.isEmpty()) {
            return 0.3;
        }
        double best = hints.stream()
                .mapToDouble(hint -> LocatorSimilarityEngine.similarity(candidateValue, hint))
                .max()
                .orElse(0.0);
        String normalizedCandidate = normalizeToken(candidateValue);
        for (String hint : hints) {
            String normalizedHint = normalizeToken(hint);
            if (normalizedHint.length() >= 3 && normalizedCandidate.contains(normalizedHint)) {
                best = Math.max(best, 0.9);
            }
        }
        return Math.min(1.0, best);
    }

    private static String normalizeToken(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private LocatorConfidence mapConfidence(double score, LocatorType type) {
        if (type == LocatorType.TEST_ID && score >= 0.75) {
            return LocatorConfidence.HIGH;
        }
        if (score >= config.confidenceHighThreshold()) {
            return LocatorConfidence.HIGH;
        }
        if (score >= config.confidenceMediumThreshold()) {
            return LocatorConfidence.MEDIUM;
        }
        return LocatorConfidence.LOW;
    }

    private static LocatorRisk mapRisk(LocatorType type) {
        return switch (type) {
            case TEST_ID -> LocatorRisk.LOW;
            case ARIA_LABEL, ROLE -> LocatorRisk.MEDIUM;
            case TEXT, CSS, UNKNOWN -> LocatorRisk.HIGH;
        };
    }

    private static String buildReasoning(LocatorFailureContext context, ScoredCandidate best) {
        return String.format(
                "Matched <%s> via %s (similarity %.2f) against old locator `%s`. "
                        + "Levenshtein-ranked candidate from DOM snapshot.",
                best.element().getTagName(),
                best.type().name().toLowerCase().replace('_', '-'),
                best.score(),
                context.getOldLocator());
    }

    private static String defaultRole(String tag) {
        return switch (tag) {
            case "button" -> "button";
            case "a" -> "link";
            case "input" -> "textbox";
            case "textarea" -> "textbox";
            case "h1", "h2", "h3" -> "heading";
            default -> null;
        };
    }

    private static String escape(String value) {
        return value.replace("'", "\\'");
    }

    private static String pathToString(java.nio.file.Path path) {
        return path == null ? "—" : path.toString();
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record ScoredCandidate(
            String locatorExpression,
            LocatorType type,
            double score,
            DomElement element
    ) {
    }
}
