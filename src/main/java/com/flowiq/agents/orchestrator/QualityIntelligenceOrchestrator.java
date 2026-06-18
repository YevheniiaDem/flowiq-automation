package com.flowiq.agents.orchestrator;

import com.flowiq.agents.orchestrator.aggregate.QualityDimensionAggregator;
import com.flowiq.agents.orchestrator.config.QualityIntelligenceConfig;
import com.flowiq.agents.orchestrator.model.QualityAgentResultsBundle;
import com.flowiq.agents.orchestrator.model.QualityAgentRunResult;
import com.flowiq.agents.orchestrator.model.QualityAgentType;
import com.flowiq.agents.orchestrator.model.QualityCategory;
import com.flowiq.agents.orchestrator.model.QualityDimensionSummary;
import com.flowiq.agents.orchestrator.model.QualityIntelligenceResult;
import com.flowiq.agents.orchestrator.report.QualityIntelligenceReportGenerator;
import com.flowiq.agents.orchestrator.runner.QualityAgentRunner;
import com.flowiq.agents.orchestrator.runner.QualityAgentRunnerFactory;
import com.flowiq.agents.orchestrator.scorer.QualityScoreCalculator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Coordinates all FlowIQ AI quality agents and produces a unified executive report.
 */
@Slf4j
public class QualityIntelligenceOrchestrator {

    private final QualityIntelligenceConfig config;
    private final List<QualityAgentRunner> runners;
    private final QualityDimensionAggregator dimensionAggregator;
    private final QualityScoreCalculator scoreCalculator;
    private final QualityIntelligenceReportGenerator reportGenerator;

    public QualityIntelligenceOrchestrator() {
        this(ConfigFactory.create(QualityIntelligenceConfig.class), QualityAgentRunnerFactory.defaultRunners());
    }

    public QualityIntelligenceOrchestrator(QualityIntelligenceConfig config, List<QualityAgentRunner> runners) {
        this.config = config;
        this.runners = List.copyOf(runners);
        this.dimensionAggregator = new QualityDimensionAggregator();
        this.scoreCalculator = new QualityScoreCalculator();
        this.reportGenerator = new QualityIntelligenceReportGenerator(config);
    }

    public QualityIntelligenceResult run() {
        log.info("Starting QualityIntelligenceOrchestrator with {} agent runner(s)", runners.size());
        Set<QualityAgentType> enabled = resolveEnabledAgents();

        QualityAgentResultsBundle bundle = new QualityAgentResultsBundle();
        List<QualityAgentRunResult> runs = new ArrayList<>();
        int failed = 0;

        for (QualityAgentRunner runner : runners) {
            if (!enabled.contains(runner.agentType())) {
                log.info("Skipping disabled agent: {}", runner.agentType());
                continue;
            }
            log.info("Running agent: {}", runner.agentType());
            QualityAgentRunResult result = runner.run();
            runs.add(result);
            if (result.isSuccess()) {
                bundle.add(result);
                log.info("Agent {} completed in {} ms", runner.agentType(), result.getDurationMs());
            } else {
                failed++;
                log.warn("Agent {} failed: {}", runner.agentType(), result.getMessage());
                if (!config.continueOnFailure()) {
                    break;
                }
            }
        }

        List<QualityDimensionSummary> dimensions = dimensionAggregator.aggregate(bundle);
        int qualityScore = scoreCalculator.calculate(dimensions);
        QualityCategory category = scoreCalculator.categorize(qualityScore);

        var resultBuilder = QualityIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .qualityScore(qualityScore)
                .category(category)
                .agentsSucceeded(runs.size() - failed)
                .agentsFailed(failed);
        dimensions.forEach(resultBuilder::dimension);
        runs.forEach(resultBuilder::agentRun);
        buildExecutiveSummary(qualityScore, category, dimensions, runs).forEach(resultBuilder::summaryLine);
        QualityIntelligenceResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Quality intelligence orchestration complete. Score={}, category={}, report={}",
                qualityScore, category, reportPath.toAbsolutePath());
        return result;
    }

    public QualityIntelligenceResult run(QualityAgentResultsBundle bundle, List<QualityAgentRunResult> runs) {
        List<QualityDimensionSummary> dimensions = dimensionAggregator.aggregate(bundle);
        int qualityScore = scoreCalculator.calculate(dimensions);
        QualityCategory category = scoreCalculator.categorize(qualityScore);
        int failed = (int) runs.stream().filter(r -> !r.isSuccess()).count();

        var resultBuilder = QualityIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .qualityScore(qualityScore)
                .category(category)
                .agentsSucceeded(runs.size() - failed)
                .agentsFailed(failed);
        dimensions.forEach(resultBuilder::dimension);
        runs.forEach(resultBuilder::agentRun);
        buildExecutiveSummary(qualityScore, category, dimensions, runs).forEach(resultBuilder::summaryLine);
        QualityIntelligenceResult result = resultBuilder.build();
        reportGenerator.generate(result);
        return result;
    }

    private Set<QualityAgentType> resolveEnabledAgents() {
        String configured = config.enabledAgents();
        if (configured == null || configured.isBlank() || "all".equalsIgnoreCase(configured.trim())) {
            return Arrays.stream(QualityAgentType.values()).collect(Collectors.toSet());
        }
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> QualityAgentType.valueOf(s.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }

    private static List<String> buildExecutiveSummary(int qualityScore,
                                                      QualityCategory category,
                                                      List<QualityDimensionSummary> dimensions,
                                                      List<QualityAgentRunResult> runs) {
        List<String> summary = new ArrayList<>();
        summary.add("Platform quality score: " + qualityScore + "/100 (" + category.name().replace('_', ' ') + ").");
        summary.add(runs.size() + " agent(s) executed in this orchestration run.");
        dimensions.stream()
                .min((a, b) -> Integer.compare(a.getHealthScore(), b.getHealthScore()))
                .ifPresent(weakest -> summary.add("Weakest dimension: " + weakest.getName()
                        + " (" + weakest.getHealthScore() + "/100)."));
        long failed = runs.stream().filter(r -> !r.isSuccess()).count();
        if (failed > 0) {
            summary.add(failed + " agent(s) failed — review execution log for details.");
        }
        return summary;
    }

    public static void main(String[] args) {
        new QualityIntelligenceOrchestrator().run();
    }
}
