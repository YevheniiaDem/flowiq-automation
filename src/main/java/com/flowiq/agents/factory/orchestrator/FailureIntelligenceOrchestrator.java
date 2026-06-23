package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.factory.config.FailureIntelligenceConfig;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import com.flowiq.agents.factory.report.FailureIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline.ExecutionOutcome;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunnerFactory;
import com.flowiq.agents.factory.scorer.FailureIntelligenceScorer;
import com.flowiq.agents.factory.scorer.FailureIntelligenceScorer.FailureMetrics;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FailureIntelligenceOrchestrator {

    private final FailureIntelligenceConfig config;
    private final List<FactoryAgentRunner> runners;
    private final FailureIntelligenceScorer scorer;
    private final FailureIntelligenceReportGenerator reportGenerator;

    public FailureIntelligenceOrchestrator() {
        this(ConfigFactory.create(FailureIntelligenceConfig.class),
                FactoryAgentRunnerFactory.failureIntelligenceRunners());
    }

    public FailureIntelligenceOrchestrator(FailureIntelligenceConfig config, List<FactoryAgentRunner> runners) {
        this.config = config;
        this.runners = List.copyOf(runners);
        this.scorer = new FailureIntelligenceScorer();
        this.reportGenerator = new FailureIntelligenceReportGenerator(config);
    }

    public FailureIntelligenceResult run() {
        log.info("Starting FailureIntelligenceOrchestrator with {} agent(s)", runners.size());
        ExecutionOutcome outcome = FactoryAgentExecutionPipeline.execute(
                runners, FactoryAgentExecutionPipeline.continueOnFailure(config));

        FailureMetrics metrics = scorer.calculateMetrics(outcome.bundle());
        int score = scorer.calculateScore(metrics);
        List<String> risks = scorer.topRisks(outcome.bundle());
        List<String> actions = scorer.recommendedActions(outcome.bundle(), metrics);

        var builder = FailureIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .failureIntelligenceScore(score)
                .flakinessMetric(metrics.flakinessMetric())
                .rootCauseConfidenceMetric(metrics.rootCauseConfidenceMetric())
                .locatorRecoveryPotentialMetric(metrics.locatorRecoveryPotentialMetric())
                .agentsSucceeded(outcome.succeededCount())
                .agentsFailed(outcome.failedCount());
        outcome.runs().forEach(builder::agentRun);
        risks.forEach(builder::riskLine);
        actions.forEach(builder::actionLine);
        buildExecutiveSummary(score, metrics, outcome).forEach(builder::summaryLine);
        FailureIntelligenceResult result = builder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Failure intelligence complete. Score={}, report={}", score, reportPath.toAbsolutePath());
        return result;
    }

    private static List<String> buildExecutiveSummary(int score,
                                                      FailureMetrics metrics,
                                                      ExecutionOutcome outcome) {
        List<String> summary = new ArrayList<>();
        summary.add("Failure Intelligence Score: " + score + "/100.");
        summary.add("Flakiness: " + metrics.flakinessMetric() + "/100, Root Cause Confidence: "
                + metrics.rootCauseConfidenceMetric() + "/100, Locator Recovery: "
                + metrics.locatorRecoveryPotentialMetric() + "/100.");
        summary.add(outcome.runs().size() + " failure intelligence agent(s) executed.");
        return summary;
    }
}
