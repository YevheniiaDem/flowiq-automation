package com.flowiq.agents.factory;

import com.flowiq.agents.factory.aggregate.FactoryDimensionAggregator;
import com.flowiq.agents.factory.config.AiQualityFactoryConfig;
import com.flowiq.agents.factory.model.AiQualityFactoryResult;
import com.flowiq.agents.factory.model.FactoryAgentRunResult;
import com.flowiq.agents.factory.model.FactoryCategory;
import com.flowiq.agents.factory.model.FactoryDimensionSummary;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import com.flowiq.agents.factory.model.GovernanceIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceResult;
import com.flowiq.agents.factory.orchestrator.FailureIntelligenceOrchestrator;
import com.flowiq.agents.factory.orchestrator.GovernanceIntelligenceOrchestrator;
import com.flowiq.agents.factory.orchestrator.PrIntelligenceOrchestrator;
import com.flowiq.agents.factory.report.AiQualityFactoryReportGenerator;
import com.flowiq.agents.factory.runner.FactoryAgentExecutionPipeline;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunnerFactory;
import com.flowiq.agents.factory.scorer.QualityFactoryScoreCalculator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Level 3 AI Quality Factory — coordinates domain orchestrators and standalone agents
 * into a unified executive quality dashboard.
 */
@Slf4j
public class AiQualityFactory {

    private final AiQualityFactoryConfig config;
    private final PrIntelligenceOrchestrator prIntelligenceOrchestrator;
    private final GovernanceIntelligenceOrchestrator governanceIntelligenceOrchestrator;
    private final FailureIntelligenceOrchestrator failureIntelligenceOrchestrator;
    private final FactoryAgentRunner releaseRiskRunner;
    private final FactoryAgentRunner smartTestGeneratorRunner;
    private final FactoryDimensionAggregator dimensionAggregator;
    private final QualityFactoryScoreCalculator scoreCalculator;
    private final AiQualityFactoryReportGenerator reportGenerator;

    public AiQualityFactory() {
        this(
                ConfigFactory.create(AiQualityFactoryConfig.class),
                new PrIntelligenceOrchestrator(),
                new GovernanceIntelligenceOrchestrator(),
                new FailureIntelligenceOrchestrator(),
                FactoryAgentRunnerFactory.releaseRiskRunner(),
                FactoryAgentRunnerFactory.smartTestGeneratorRunner(),
                new FactoryDimensionAggregator(),
                new QualityFactoryScoreCalculator(),
                new AiQualityFactoryReportGenerator(ConfigFactory.create(AiQualityFactoryConfig.class)));
    }

    public AiQualityFactory(AiQualityFactoryConfig config,
                            PrIntelligenceOrchestrator prIntelligenceOrchestrator,
                            GovernanceIntelligenceOrchestrator governanceIntelligenceOrchestrator,
                            FailureIntelligenceOrchestrator failureIntelligenceOrchestrator,
                            FactoryAgentRunner releaseRiskRunner,
                            FactoryAgentRunner smartTestGeneratorRunner,
                            FactoryDimensionAggregator dimensionAggregator,
                            QualityFactoryScoreCalculator scoreCalculator,
                            AiQualityFactoryReportGenerator reportGenerator) {
        this.config = config;
        this.prIntelligenceOrchestrator = prIntelligenceOrchestrator;
        this.governanceIntelligenceOrchestrator = governanceIntelligenceOrchestrator;
        this.failureIntelligenceOrchestrator = failureIntelligenceOrchestrator;
        this.releaseRiskRunner = releaseRiskRunner;
        this.smartTestGeneratorRunner = smartTestGeneratorRunner;
        this.dimensionAggregator = dimensionAggregator;
        this.scoreCalculator = scoreCalculator;
        this.reportGenerator = reportGenerator;
    }

    public AiQualityFactoryResult run() {
        log.info("Starting AiQualityFactory — hierarchical quality engineering pipeline");

        PrIntelligenceResult pr = prIntelligenceOrchestrator.run();
        GovernanceIntelligenceResult governance = governanceIntelligenceOrchestrator.run();
        FailureIntelligenceResult failure = failureIntelligenceOrchestrator.run();

        FactoryAgentRunResult releaseRun = runStandalone(releaseRiskRunner);
        FactoryAgentRunResult generatorRun = runStandalone(smartTestGeneratorRunner);

        List<FactoryDimensionSummary> dimensions = dimensionAggregator.aggregate(
                pr, governance, failure, releaseRun, generatorRun);
        int overallScore = scoreCalculator.calculate(dimensions);
        FactoryCategory category = scoreCalculator.categorize(overallScore);

        Set<String> risks = new LinkedHashSet<>();
        pr.getTopRisks().forEach(risks::add);
        governance.getTopRisks().forEach(risks::add);
        failure.getTopRisks().forEach(risks::add);

        Set<String> actions = new LinkedHashSet<>();
        pr.getRecommendedActions().forEach(actions::add);
        governance.getRecommendedActions().forEach(actions::add);
        failure.getRecommendedActions().forEach(actions::add);

        var builder = AiQualityFactoryResult.builder()
                .analyzedAt(Instant.now())
                .overallScore(overallScore)
                .category(category)
                .prIntelligence(pr)
                .governanceIntelligence(governance)
                .failureIntelligence(failure)
                .releaseRiskRun(releaseRun)
                .smartTestGeneratorRun(generatorRun);
        dimensions.forEach(builder::dimension);
        risks.forEach(builder::riskLine);
        actions.forEach(builder::actionLine);
        buildExecutiveSummary(overallScore, category, dimensions).forEach(builder::summaryLine);
        AiQualityFactoryResult result = builder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("AI Quality Factory complete. Score={}, category={}, report={}",
                overallScore, category, reportPath.toAbsolutePath());
        return result;
    }

    private FactoryAgentRunResult runStandalone(FactoryAgentRunner runner) {
        FactoryAgentExecutionPipeline.ExecutionOutcome outcome = FactoryAgentExecutionPipeline.execute(
                runner, FactoryAgentExecutionPipeline.continueOnFailure(config));
        return outcome.runs().isEmpty()
                ? FactoryAgentRunResult.failure(runner.agentType(), "No execution result", 0)
                : outcome.runs().get(0);
    }

    private static List<String> buildExecutiveSummary(int score,
                                                      FactoryCategory category,
                                                      List<FactoryDimensionSummary> dimensions) {
        List<String> summary = new ArrayList<>();
        summary.add("AI Quality Factory Score: " + score + "/100 (" + category.name().replace('_', ' ') + ").");
        summary.add("Five factory dimensions aggregated across PR, governance, failure, release, and test intelligence.");
        dimensions.stream()
                .min((a, b) -> Integer.compare(a.getHealthScore(), b.getHealthScore()))
                .ifPresent(weakest -> summary.add("Weakest dimension: " + weakest.getName()
                        + " (" + weakest.getHealthScore() + "/100)."));
        return summary;
    }

    public static void main(String[] args) {
        new AiQualityFactory().run();
    }
}
