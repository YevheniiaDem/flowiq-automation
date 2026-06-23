package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.factory.config.PrIntelligenceConfig;
import com.flowiq.agents.factory.model.PrIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceVerdict;
import com.flowiq.agents.factory.report.PrIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline.ExecutionOutcome;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunnerFactory;
import com.flowiq.agents.factory.scorer.PrIntelligenceScorer;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PrIntelligenceOrchestrator {

    private final PrIntelligenceConfig config;
    private final List<FactoryAgentRunner> runners;
    private final PrIntelligenceScorer scorer;
    private final PrIntelligenceReportGenerator reportGenerator;

    public PrIntelligenceOrchestrator() {
        this(ConfigFactory.create(PrIntelligenceConfig.class), FactoryAgentRunnerFactory.prIntelligenceRunners());
    }

    public PrIntelligenceOrchestrator(PrIntelligenceConfig config, List<FactoryAgentRunner> runners) {
        this.config = config;
        this.runners = List.copyOf(runners);
        this.scorer = new PrIntelligenceScorer();
        this.reportGenerator = new PrIntelligenceReportGenerator(config);
    }

    public PrIntelligenceResult run() {
        log.info("Starting PrIntelligenceOrchestrator with {} agent(s)", runners.size());
        ExecutionOutcome outcome = FactoryAgentExecutionPipeline.execute(
                runners, FactoryAgentExecutionPipeline.continueOnFailure(config));

        int score = scorer.calculateScore(outcome.bundle());
        PrIntelligenceVerdict verdict = scorer.determineVerdict(outcome.bundle(), score);
        List<String> risks = scorer.topRisks(outcome.bundle());
        List<String> actions = scorer.recommendedActions(outcome.bundle(), verdict);

        var builder = PrIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .prQualityScore(score)
                .verdict(verdict)
                .agentsSucceeded(outcome.succeededCount())
                .agentsFailed(outcome.failedCount());
        outcome.runs().forEach(builder::agentRun);
        risks.forEach(builder::riskLine);
        actions.forEach(builder::actionLine);
        buildExecutiveSummary(score, verdict, outcome).forEach(builder::summaryLine);
        PrIntelligenceResult result = builder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("PR intelligence complete. Score={}, verdict={}, report={}",
                score, verdict, reportPath.toAbsolutePath());
        return result;
    }

    private static List<String> buildExecutiveSummary(int score,
                                                      PrIntelligenceVerdict verdict,
                                                      ExecutionOutcome outcome) {
        List<String> summary = new ArrayList<>();
        summary.add("PR Quality Score: " + score + "/100 (" + verdict.name().replace('_', ' ') + ").");
        summary.add(outcome.runs().size() + " specialized PR agent(s) executed.");
        if (outcome.failedCount() > 0) {
            summary.add(outcome.failedCount() + " agent(s) failed — review execution log.");
        }
        return summary;
    }
}
