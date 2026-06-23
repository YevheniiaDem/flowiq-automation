package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.factory.config.FailureIntelligenceConfig;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import com.flowiq.agents.factory.report.FailureIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.AbstractFactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.scorer.FailureIntelligenceScorer;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.rootcause.model.RootCauseCategory;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import com.flowiq.agents.selfhealing.model.LocatorConfidence;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import com.flowiq.agents.selfhealing.model.LocatorType;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FailureIntelligenceOrchestratorTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/failure-intelligence-test.properties")
    interface TestFailureConfig extends FailureIntelligenceConfig {
    }

    private static FailureIntelligenceConfig testConfig() {
        return ConfigFactory.create(TestFailureConfig.class);
    }

    @Test(groups = "unit")
    public void failureScorerShouldComputeMetricsAndScore() {
        var bundle = new com.flowiq.agents.factory.model.FactoryAgentResultsBundle();
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.FLAKY_TEST_INVESTIGATOR,
                FlakyInvestigationResult.builder().portfolioFlakinessPercent(8.0).flakyTestCount(1).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.ROOT_CAUSE_ANALYSIS,
                RootCauseAnalysisResult.builder()
                        .finding(RootCauseFinding.builder()
                                .mostProbableRootCause(RootCauseCategory.UI_BUG)
                                .confidence(85)
                                .build())
                        .build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.SELF_HEALING_LOCATOR,
                SelfHealingResult.builder()
                        .suggestion(LocatorSuggestion.builder()
                                .confidence(LocatorConfidence.HIGH)
                                .suggestedLocatorType(LocatorType.TEST_ID)
                                .similarityScore(0.9)
                                .build())
                        .suggestionsGenerated(1)
                        .build(), 1));

        FailureIntelligenceScorer scorer = new FailureIntelligenceScorer();
        var metrics = scorer.calculateMetrics(bundle);
        int score = scorer.calculateScore(metrics);

        assertThat(metrics.flakinessMetric()).isGreaterThan(80);
        assertThat(metrics.rootCauseConfidenceMetric()).isEqualTo(85);
        assertThat(score).isBetween(75, 100);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainMetricsTable() throws Exception {
        FailureIntelligenceResult result = FailureIntelligenceResult.builder()
                .analyzedAt(Instant.parse("2026-06-18T12:00:00Z"))
                .failureIntelligenceScore(84)
                .flakinessMetric(88)
                .rootCauseConfidenceMetric(80)
                .locatorRecoveryPotentialMetric(75)
                .agentsSucceeded(3)
                .agentsFailed(0)
                .summaryLine("Failure Intelligence Score: 84/100.")
                .build();

        String content = Files.readString(new FailureIntelligenceReportGenerator(testConfig()).generate(result));

        assertThat(content).contains("# Failure Intelligence Report");
        assertThat(content).contains("## Metrics");
        assertThat(content).contains("Flakiness");
        assertThat(content).contains("Root Cause Confidence");
        assertThat(content).contains("Locator Recovery Potential");
    }

    @Test(groups = "unit")
    public void orchestratorShouldRunStubAgentsAndProduceScore() {
        List<FactoryAgentRunner> runners = List.of(
                stubRunner(FactoryAgentType.ROOT_CAUSE_ANALYSIS,
                        RootCauseAnalysisResult.builder().failuresAnalyzed(2).highConfidenceFindings(1).build()),
                stubRunner(FactoryAgentType.FLAKY_TEST_INVESTIGATOR,
                        FlakyInvestigationResult.builder().portfolioFlakinessPercent(5.0).flakyTestCount(1).build()),
                stubRunner(FactoryAgentType.SELF_HEALING_LOCATOR,
                        SelfHealingResult.builder().suggestionsGenerated(1).failuresAnalyzed(1).build()));

        FailureIntelligenceResult result = new FailureIntelligenceOrchestrator(testConfig(), runners).run();

        assertThat(result.getFailureIntelligenceScore()).isBetween(50, 100);
        assertThat(result.getFlakinessMetric()).isPositive();
        assertThat(result.getAgentsSucceeded()).isEqualTo(3);
    }

    private static FactoryAgentRunner stubRunner(FactoryAgentType type, Object payload) {
        return new AbstractFactoryAgentRunner(type) {
            @Override
            protected Object execute() {
                return payload;
            }
        };
    }
}
