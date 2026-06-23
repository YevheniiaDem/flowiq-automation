package com.flowiq.agents.factory;

import com.flowiq.agents.factory.aggregate.FactoryDimensionAggregator;
import com.flowiq.agents.factory.config.AiQualityFactoryConfig;
import com.flowiq.agents.factory.config.FailureIntelligenceConfig;
import com.flowiq.agents.factory.config.GovernanceIntelligenceConfig;
import com.flowiq.agents.factory.config.PrIntelligenceConfig;
import com.flowiq.agents.factory.model.AiQualityFactoryResult;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.FactoryCategory;
import com.flowiq.agents.factory.model.FactoryDimensionSummary;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import com.flowiq.agents.factory.model.GovernanceCategory;
import com.flowiq.agents.factory.model.GovernanceIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceVerdict;
import com.flowiq.agents.factory.orchestrator.FailureIntelligenceOrchestrator;
import com.flowiq.agents.factory.orchestrator.GovernanceIntelligenceOrchestrator;
import com.flowiq.agents.factory.orchestrator.PrIntelligenceOrchestrator;
import com.flowiq.agents.factory.report.AiQualityFactoryReportGenerator;
import com.flowiq.agents.factory.runner.AbstractFactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.scorer.QualityFactoryScoreCalculator;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.release.model.ReleaseRecommendation;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AiQualityFactoryTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/ai-quality-factory-test.properties")
    interface TestFactoryConfig extends AiQualityFactoryConfig {
    }

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/pr-intelligence-test.properties")
    interface TestPrConfig extends PrIntelligenceConfig {
    }

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/governance-intelligence-test.properties")
    interface TestGovernanceConfig extends GovernanceIntelligenceConfig {
    }

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/failure-intelligence-test.properties")
    interface TestFailureConfig extends FailureIntelligenceConfig {
    }

    private static AiQualityFactoryConfig testConfig() {
        return ConfigFactory.create(TestFactoryConfig.class);
    }

    @Test(groups = "unit")
    public void factoryCategoryShouldMapScoreRanges() {
        assertThat(FactoryCategory.fromScore(90)).isEqualTo(FactoryCategory.EXCELLENT);
        assertThat(FactoryCategory.fromScore(75)).isEqualTo(FactoryCategory.GOOD);
        assertThat(FactoryCategory.fromScore(55)).isEqualTo(FactoryCategory.NEEDS_ATTENTION);
        assertThat(FactoryCategory.fromScore(30)).isEqualTo(FactoryCategory.CRITICAL);
    }

    @Test(groups = "unit")
    public void qualityFactoryScoreCalculatorShouldApplyWeights() {
        List<FactoryDimensionSummary> dimensions = List.of(
                FactoryDimensionSummary.builder().name("PR Health").healthScore(80).build(),
                FactoryDimensionSummary.builder().name("Governance Health").healthScore(70).build(),
                FactoryDimensionSummary.builder().name("Failure Intelligence").healthScore(85).build(),
                FactoryDimensionSummary.builder().name("Release Readiness").healthScore(75).build(),
                FactoryDimensionSummary.builder().name("Test Intelligence").healthScore(90).build());

        int score = new QualityFactoryScoreCalculator().calculate(dimensions);
        assertThat(score).isBetween(75, 82);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainExecutiveDashboardSections() throws Exception {
        AiQualityFactoryResult result = AiQualityFactoryResult.builder()
                .analyzedAt(Instant.parse("2026-06-18T12:00:00Z"))
                .overallScore(79)
                .category(FactoryCategory.GOOD)
                .dimension(FactoryDimensionSummary.builder().name("PR Health").healthScore(82).build())
                .dimension(FactoryDimensionSummary.builder().name("Governance Health").healthScore(76).build())
                .summaryLine("AI Quality Factory Score: 79/100.")
                .riskLine("Flaky tests detected")
                .actionLine("Stabilize flaky tests")
                .build();

        String content = Files.readString(new AiQualityFactoryReportGenerator(testConfig()).generate(result));

        assertThat(content).contains("# AI Quality Factory Report");
        assertThat(content).contains("## Overall Score");
        assertThat(content).contains("## Executive Summary");
        assertThat(content).contains("## Factory Dimensions");
        assertThat(content).contains("PR Health");
        assertThat(content).contains("Governance Health");
        assertThat(content).contains("## Top Risks");
        assertThat(content).contains("## Recommended Actions");
    }

    @Test(groups = "unit")
    public void factoryShouldAggregateDomainOrchestratorsWithStubAgents() {
        PrIntelligenceResult prResult = PrIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .prQualityScore(85)
                .verdict(PrIntelligenceVerdict.APPROVED)
                .agentsSucceeded(4)
                .build();
        GovernanceIntelligenceResult governanceResult = GovernanceIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .governanceHealthScore(80)
                .category(GovernanceCategory.GOOD)
                .agentsSucceeded(4)
                .build();
        FailureIntelligenceResult failureResult = FailureIntelligenceResult.builder()
                .analyzedAt(Instant.now())
                .failureIntelligenceScore(88)
                .flakinessMetric(90)
                .rootCauseConfidenceMetric(85)
                .locatorRecoveryPotentialMetric(80)
                .agentsSucceeded(3)
                .build();

        AiQualityFactory factory = factoryWithStubOrchestrators(prResult, governanceResult, failureResult);
        AiQualityFactoryResult result = factory.run();

        assertThat(result.getOverallScore()).isBetween(70, 95);
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getDimensions()).hasSize(5);
        assertThat(result.getDimensions()).extracting(FactoryDimensionSummary::getName)
                .containsExactly(
                        "PR Health",
                        "Governance Health",
                        "Failure Intelligence",
                        "Release Readiness",
                        "Test Intelligence");
    }

    private static AiQualityFactory factoryWithStubOrchestrators(
            PrIntelligenceResult prResult,
            GovernanceIntelligenceResult governanceResult,
            FailureIntelligenceResult failureResult) {
        return new AiQualityFactory(
                testConfig(),
                stubPrOrchestrator(prResult),
                stubGovernanceOrchestrator(governanceResult),
                stubFailureOrchestrator(failureResult),
                releaseRunner(),
                generatorRunner(),
                new FactoryDimensionAggregator(),
                new QualityFactoryScoreCalculator(),
                new AiQualityFactoryReportGenerator(testConfig()));
    }

    private static PrIntelligenceOrchestrator stubPrOrchestrator(PrIntelligenceResult result) {
        return new PrIntelligenceOrchestrator(ConfigFactory.create(TestPrConfig.class), List.of()) {
            @Override
            public PrIntelligenceResult run() {
                return result;
            }
        };
    }

    private static GovernanceIntelligenceOrchestrator stubGovernanceOrchestrator(
            GovernanceIntelligenceResult result) {
        return new GovernanceIntelligenceOrchestrator(ConfigFactory.create(TestGovernanceConfig.class), List.of()) {
            @Override
            public GovernanceIntelligenceResult run() {
                return result;
            }
        };
    }

    private static FailureIntelligenceOrchestrator stubFailureOrchestrator(FailureIntelligenceResult result) {
        return new FailureIntelligenceOrchestrator(ConfigFactory.create(TestFailureConfig.class), List.of()) {
            @Override
            public FailureIntelligenceResult run() {
                return result;
            }
        };
    }

    private static FactoryAgentRunner releaseRunner() {
        return new AbstractFactoryAgentRunner(FactoryAgentType.RELEASE_RISK_ASSESSMENT) {
            @Override
            protected Object execute() {
                return ReleaseRiskAssessmentResult.builder()
                        .releaseRiskScore(20.0)
                        .riskCategory(ReleaseRiskCategory.GREEN)
                        .recommendation(ReleaseRecommendation.APPROVE_RELEASE)
                        .build();
            }
        };
    }

    private static FactoryAgentRunner generatorRunner() {
        return new AbstractFactoryAgentRunner(FactoryAgentType.SMART_TEST_GENERATOR) {
            @Override
            protected Object execute() {
                return ScenarioGenerationResult.builder()
                        .endpointsAnalyzed(10)
                        .schemasLoaded(5)
                        .build();
            }
        };
    }
}
