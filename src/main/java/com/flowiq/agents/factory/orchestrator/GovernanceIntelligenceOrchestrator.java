package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.factory.config.GovernanceIntelligenceConfig;
import com.flowiq.agents.factory.model.GovernanceCategory;
import com.flowiq.agents.factory.model.GovernanceIntelligenceResult;
import com.flowiq.agents.factory.report.GovernanceIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline.ExecutionOutcome;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunnerFactory;
import com.flowiq.agents.factory.scorer.GovernanceIntelligenceScorer;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GovernanceIntelligenceOrchestrator {

    private final GovernanceIntelligenceConfig config;
    private final List<FactoryAgentRunner> runners;
    private final GovernanceIntelligenceScorer scorer;
    private final GovernanceIntelligenceReportGenerator reportGenerator;

    public GovernanceIntelligenceOrchestrator() {
        this(ConfigFactory.create(GovernanceIntelligenceConfig.class),
                FactoryAgentRunnerFactory.governanceIntelligenceRunners());
    }

    public GovernanceIntelligenceOrchestrator(GovernanceIntelligenceConfig config, List<FactoryAgentRunner> runners) {
        this.config = config;
        this.runners = List.copyOf(runners);
        this.scorer = new GovernanceIntelligenceScorer();
        this.reportGenerator = new GovernanceIntelligenceReportGenerator(config);
    }

    public GovernanceIntelligenceResult run() {
        log.info("Starting GovernanceIntelligenceOrchestrator with {} agent(s)", runners.size());
        ExecutionOutcome outcome = FactoryAgentExecutionPipeline.execute(
                runners, FactoryAgentExecutionPipeline.continueOnFailure(config));

        int score = scorer.calculateScore(outcome.bundle());
        GovernanceCategory category = scorer.categorize(score);
        List<String> risks = scorer.topRisks(outcome.bundle());
        List<String> actions = scorer.recommendedActions(outcome.bundle(), category);

        var builder = GovernanceIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .governanceHealthScore(score)
                .category(category)
                .agentsSucceeded(outcome.succeededCount())
                .agentsFailed(outcome.failedCount());
        outcome.runs().forEach(builder::agentRun);
        risks.forEach(builder::riskLine);
        actions.forEach(builder::actionLine);
        buildExecutiveSummary(score, category, outcome).forEach(builder::summaryLine);
        GovernanceIntelligenceResult result = builder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Governance intelligence complete. Score={}, category={}, report={}",
                score, category, reportPath.toAbsolutePath());
        return result;
    }

    private static List<String> buildExecutiveSummary(int score,
                                                      GovernanceCategory category,
                                                      ExecutionOutcome outcome) {
        List<String> summary = new ArrayList<>();
        summary.add("Governance Health Score: " + score + "/100 (" + category.name() + ").");
        summary.add(outcome.runs().size() + " governance agent(s) executed.");
        if (outcome.failedCount() > 0) {
            summary.add(outcome.failedCount() + " agent(s) failed — review execution log.");
        }
        return summary;
    }
}
