package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.factory.config.GovernanceIntelligenceConfig;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.GovernanceCategory;
import com.flowiq.agents.factory.model.GovernanceIntelligenceResult;
import com.flowiq.agents.factory.report.GovernanceIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.AbstractFactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.scorer.GovernanceIntelligenceScorer;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.maintenance.model.MaintenanceHealthCategory;
import com.flowiq.agents.maintenance.model.TestMaintenanceResult;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GovernanceIntelligenceOrchestratorTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/governance-intelligence-test.properties")
    interface TestGovernanceConfig extends GovernanceIntelligenceConfig {
    }

    private static GovernanceIntelligenceConfig testConfig() {
        return ConfigFactory.create(TestGovernanceConfig.class);
    }

    @Test(groups = "unit")
    public void governanceCategoryShouldMapScoreRanges() {
        assertThat(GovernanceCategory.fromScore(90)).isEqualTo(GovernanceCategory.EXCELLENT);
        assertThat(GovernanceCategory.fromScore(75)).isEqualTo(GovernanceCategory.GOOD);
        assertThat(GovernanceCategory.fromScore(55)).isEqualTo(GovernanceCategory.WARNING);
        assertThat(GovernanceCategory.fromScore(30)).isEqualTo(GovernanceCategory.CRITICAL);
    }

    @Test(groups = "unit")
    public void governanceScorerShouldAverageComponentScores() {
        var bundle = new com.flowiq.agents.factory.model.FactoryAgentResultsBundle();
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.ARCHITECTURE_DRIFT,
                ArchitectureDriftResult.builder().architectureHealthScore(80).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.REQUIREMENTS_TRACEABILITY,
                TraceabilityAnalysisResult.builder().overallCoveragePercent(70.0).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.TEST_GAP_ANALYZER,
                TestGapAnalysisResult.builder().overallCoveragePercent(75.0).gaps(List.of()).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.TEST_MAINTENANCE,
                TestMaintenanceResult.builder().automationHealthScore(85).healthCategory(MaintenanceHealthCategory.GOOD).build(), 1));

        int score = new GovernanceIntelligenceScorer().calculateScore(bundle);
        assertThat(score).isBetween(75, 82);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        GovernanceIntelligenceResult result = GovernanceIntelligenceResult.builder()
                .analyzedAt(Instant.parse("2026-06-18T12:00:00Z"))
                .governanceHealthScore(78)
                .category(GovernanceCategory.GOOD)
                .agentsSucceeded(4)
                .agentsFailed(0)
                .summaryLine("Governance Health Score: 78/100.")
                .build();

        String content = Files.readString(new GovernanceIntelligenceReportGenerator(testConfig()).generate(result));

        assertThat(content).contains("# Governance Intelligence Report");
        assertThat(content).contains("## Governance Health Score");
        assertThat(content).contains("GOOD");
        assertThat(content).contains("## Agent Execution Log");
    }

    @Test(groups = "unit")
    public void orchestratorShouldRunStubAgentsAndProduceScore() {
        List<FactoryAgentRunner> runners = List.of(
                stubRunner(FactoryAgentType.ARCHITECTURE_DRIFT,
                        ArchitectureDriftResult.builder().architectureHealthScore(88).build()),
                stubRunner(FactoryAgentType.REQUIREMENTS_TRACEABILITY,
                        TraceabilityAnalysisResult.builder().overallCoveragePercent(80.0).featureCount(10)
                                .documentedFeatureCount(8).openApiEndpointCount(20).build()),
                stubRunner(FactoryAgentType.TEST_GAP_ANALYZER,
                        TestGapAnalysisResult.builder().overallCoveragePercent(82.0).gaps(List.of()).build()),
                stubRunner(FactoryAgentType.TEST_MAINTENANCE,
                        TestMaintenanceResult.builder().automationHealthScore(90)
                                .healthCategory(MaintenanceHealthCategory.EXCELLENT).build()));

        GovernanceIntelligenceResult result =
                new GovernanceIntelligenceOrchestrator(testConfig(), runners).run();

        assertThat(result.getGovernanceHealthScore()).isBetween(80, 90);
        assertThat(result.getCategory()).isIn(GovernanceCategory.EXCELLENT, GovernanceCategory.GOOD);
        assertThat(result.getAgentsSucceeded()).isEqualTo(4);
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
