package com.flowiq.agents.selfhealing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.selfhealing.analyzer.LocatorFailureAnalyzer;
import com.flowiq.agents.selfhealing.collector.DomSnapshotCollector;
import com.flowiq.agents.selfhealing.collector.UiFailureArtifactLoader;
import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.generator.SuggestedLocatorGenerator;
import com.flowiq.agents.selfhealing.llm.SelfHealingLlmProvider;
import com.flowiq.agents.selfhealing.llm.SelfHealingLlmProviderFactory;
import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;
import com.flowiq.agents.selfhealing.report.SelfHealingReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Production-grade agent that analyzes Playwright UI test failures and proposes
 * healed locators using DOM snapshots, Levenshtein similarity, and optional LLM enrichment.
 */
@Slf4j
public class SelfHealingLocatorAgent {

    private final SelfHealingAgentConfig config;
    private final UiFailureArtifactLoader artifactLoader;
    private final LocatorFailureAnalyzer failureAnalyzer;
    private final SuggestedLocatorGenerator suggestionGenerator;
    private final SelfHealingReportGenerator reportGenerator;

    public SelfHealingLocatorAgent() {
        this(ConfigFactory.create(SelfHealingAgentConfig.class));
    }

    public SelfHealingLocatorAgent(SelfHealingAgentConfig config) {
        this.config = config;
        ObjectMapper objectMapper = new ObjectMapper();
        DomSnapshotCollector domCollector = new DomSnapshotCollector();
        this.artifactLoader = new UiFailureArtifactLoader(config, objectMapper, domCollector);
        this.failureAnalyzer = new LocatorFailureAnalyzer();
        SelfHealingLlmProvider llmProvider = SelfHealingLlmProviderFactory.create(config);
        this.suggestionGenerator = new SuggestedLocatorGenerator(config, failureAnalyzer, llmProvider);
        this.reportGenerator = new SelfHealingReportGenerator(config);
    }

    public SelfHealingResult run() {
        log.info("Starting SelfHealingLocatorAgent");
        return run(artifactLoader.loadFailures(), artifactLoader.summarizeSources());
    }

    public SelfHealingResult run(List<LocatorFailureContext> failures, String dataSourcesSummary) {
        List<LocatorSuggestion> suggestions = new ArrayList<>();
        int analyzed = 0;

        for (LocatorFailureContext failure : failures) {
            if (!failureAnalyzer.isLocatorFailure(failure.getFailureMessage(), failure.getStackTrace())) {
                log.debug("Skipping non-locator failure: {}", failure.getTestKey());
                continue;
            }
            analyzed++;
            suggestionGenerator.generate(failure).ifPresent(suggestions::add);
        }

        var resultBuilder = SelfHealingResult.builder()
                .analyzedAt(Instant.now())
                .failuresAnalyzed(analyzed)
                .suggestionsGenerated(suggestions.size())
                .suggestions(suggestions)
                .dataSourcesSummary(dataSourcesSummary);
        buildExecutiveSummary(analyzed, suggestions).forEach(resultBuilder::summaryLine);
        SelfHealingResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Self-healing complete. {} suggestion(s), report={}",
                suggestions.size(), reportPath.toAbsolutePath());
        return result;
    }

    private List<String> buildExecutiveSummary(int analyzed, List<LocatorSuggestion> suggestions) {
        List<String> summary = new ArrayList<>();
        summary.add(String.format("Analyzed %d Playwright locator failure(s); produced %d suggestion(s).",
                analyzed, suggestions.size()));
        long high = suggestions.stream().filter(s -> s.getConfidence().name().equals("HIGH")).count();
        long medium = suggestions.stream().filter(s -> s.getConfidence().name().equals("MEDIUM")).count();
        if (!suggestions.isEmpty()) {
            summary.add(String.format("Confidence distribution: %d HIGH, %d MEDIUM, %d LOW.",
                    high, medium, suggestions.size() - high - medium));
        }
        suggestions.stream()
                .filter(s -> s.getRisk().name().equals("LOW"))
                .limit(2)
                .forEach(s -> summary.add("Ready to review: `" + s.getSuggestedLocator() + "` for "
                        + s.getTestName()));
        return summary;
    }

    public static void main(String[] args) {
        new SelfHealingLocatorAgent().run();
    }
}
