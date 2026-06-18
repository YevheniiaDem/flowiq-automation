package com.flowiq.agents.orchestrator;

import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.orchestrator.aggregate.QualityDimensionAggregator;
import com.flowiq.agents.orchestrator.config.QualityIntelligenceConfig;
import com.flowiq.agents.orchestrator.model.QualityAgentResultsBundle;
import com.flowiq.agents.orchestrator.model.QualityAgentRunResult;
import com.flowiq.agents.orchestrator.model.QualityAgentType;
import com.flowiq.agents.orchestrator.model.QualityCategory;
import com.flowiq.agents.orchestrator.model.QualityDimensionSummary;
import com.flowiq.agents.orchestrator.model.QualityIntelligenceResult;
import com.flowiq.agents.orchestrator.report.QualityIntelligenceReportGenerator;
import com.flowiq.agents.orchestrator.runner.AbstractQualityAgentRunner;
import com.flowiq.agents.orchestrator.runner.QualityAgentRunner;
import com.flowiq.agents.orchestrator.scorer.QualityScoreCalculator;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.release.model.ReleaseRecommendation;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QualityIntelligenceOrchestratorTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/quality-intelligence-test.properties")
    interface TestQualityIntelligenceConfig extends QualityIntelligenceConfig {
    }

    private static QualityIntelligenceConfig testConfig() {
        return ConfigFactory.create(TestQualityIntelligenceConfig.class);
    }

    @Test(groups = "unit")
    public void qualityCategoryShouldMapScoreRanges() {
        assertThat(QualityCategory.fromScore(90)).isEqualTo(QualityCategory.EXCELLENT);
        assertThat(QualityCategory.fromScore(75)).isEqualTo(QualityCategory.GOOD);
        assertThat(QualityCategory.fromScore(55)).isEqualTo(QualityCategory.NEEDS_ATTENTION);
        assertThat(QualityCategory.fromScore(30)).isEqualTo(QualityCategory.CRITICAL);
    }

    @Test(groups = "unit")
    public void dimensionAggregatorShouldBuildAllExecutiveSections() {
        QualityAgentResultsBundle bundle = sampleBundle();
        List<QualityDimensionSummary> dimensions = new QualityDimensionAggregator().aggregate(bundle);

        assertThat(dimensions).extracting(QualityDimensionSummary::getName)
                .containsExactly(
                        "API Health",
                        "Coverage Health",
                        "Release Risk",
                        "Architecture Health",
                        "Flaky Status",
                        "Regression Risk",
                        "Traceability Status");
        assertThat(dimensions).allMatch(d -> d.getHealthScore() >= 0 && d.getHealthScore() <= 100);
    }

    @Test(groups = "unit")
    public void qualityScoreCalculatorShouldComputeWeightedScore() {
        List<QualityDimensionSummary> dimensions = List.of(
                QualityDimensionSummary.builder().name("API Health").healthScore(80).build(),
                QualityDimensionSummary.builder().name("Coverage Health").healthScore(70).build(),
                QualityDimensionSummary.builder().name("Release Risk").healthScore(60).build(),
                QualityDimensionSummary.builder().name("Architecture Health").healthScore(90).build(),
                QualityDimensionSummary.builder().name("Flaky Status").healthScore(85).build(),
                QualityDimensionSummary.builder().name("Regression Risk").healthScore(78).build(),
                QualityDimensionSummary.builder().name("Traceability Status").healthScore(72).build());

        int score = new QualityScoreCalculator().calculate(dimensions);

        assertThat(score).isBetween(70, 82);
        assertThat(new QualityScoreCalculator().categorize(score)).isIn(
                QualityCategory.GOOD, QualityCategory.EXCELLENT, QualityCategory.NEEDS_ATTENTION);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        List<QualityDimensionSummary> dimensions = new QualityDimensionAggregator()
                .aggregate(sampleBundle());

        var resultBuilder = QualityIntelligenceResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .qualityScore(78)
                .category(QualityCategory.GOOD)
                .agentsSucceeded(11)
                .agentsFailed(0)
                .agentRun(QualityAgentRunResult.success(
                        QualityAgentType.API_CHANGE_DETECTION, "ok", 120))
                .summaryLine("Platform quality score: 78/100 (GOOD).");
        dimensions.forEach(resultBuilder::dimension);
        QualityIntelligenceResult result = resultBuilder.build();

        Path reportPath = new QualityIntelligenceReportGenerator(testConfig()).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Quality Intelligence Executive Report");
        assertThat(content).contains("## Final Quality Score");
        assertThat(content).contains("## API Health");
        assertThat(content).contains("## Coverage Health");
        assertThat(content).contains("## Release Risk");
        assertThat(content).contains("## Architecture Health");
        assertThat(content).contains("## Flaky Status");
        assertThat(content).contains("## Regression Risk");
        assertThat(content).contains("## Traceability Status");
        assertThat(content).contains("## Agent Execution Log");
        assertThat(content).contains("GOOD");
    }

    @Test(groups = "unit")
    public void orchestratorShouldRunStubAgentsAndProduceScore() {
        List<QualityAgentRunner> stubRunners = List.of(
                stubRunner(QualityAgentType.API_CHANGE_DETECTION,
                        AnalysisResult.builder().riskLevel(RiskLevel.LOW).build()),
                stubRunner(QualityAgentType.TEST_GAP_ANALYZER,
                        TestGapAnalysisResult.builder().overallCoveragePercent(82.0).build()),
                stubRunner(QualityAgentType.RELEASE_RISK_ASSESSMENT,
                        ReleaseRiskAssessmentResult.builder()
                                .releaseRiskScore(25.0)
                                .riskCategory(ReleaseRiskCategory.GREEN)
                                .recommendation(ReleaseRecommendation.APPROVE_RELEASE)
                                .build()),
                stubRunner(QualityAgentType.ARCHITECTURE_DRIFT,
                        ArchitectureDriftResult.builder().architectureHealthScore(88).build()),
                stubRunner(QualityAgentType.FLAKY_TEST_INVESTIGATOR,
                        FlakyInvestigationResult.builder().portfolioFlakinessPercent(5.0).flakyTestCount(1).build()),
                stubRunner(QualityAgentType.RISK_BASED_REGRESSION,
                        RiskBasedRegressionResult.builder()
                                .recommendation(RegressionScopeRecommendation.PARTIAL_REGRESSION)
                                .build()),
                stubRunner(QualityAgentType.REQUIREMENTS_TRACEABILITY,
                        TraceabilityAnalysisResult.builder().overallCoveragePercent(80.0).featureCount(10)
                                .documentedFeatureCount(8).openApiEndpointCount(20).build()),
                stubRunner(QualityAgentType.TEST_REVIEW,
                        TestReviewResult.builder().overallVerdict(ReviewVerdict.APPROVED).build()));

        QualityIntelligenceResult result = new QualityIntelligenceOrchestrator(
                testConfig(), stubRunners).run();

        assertThat(result.getQualityScore()).isBetween(50, 100);
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getDimensions()).hasSize(7);
        assertThat(result.getAgentsSucceeded()).isEqualTo(8);
        assertThat(result.getAgentsFailed()).isZero();
    }

    @Test(groups = "unit")
    public void orchestratorShouldContinueWhenAgentFails() {
        QualityAgentRunner failing = new AbstractQualityAgentRunner(QualityAgentType.SMART_TEST_GENERATOR) {
            @Override
            protected Object execute() {
                throw new IllegalStateException("simulated failure");
            }
        };
        QualityAgentRunner ok = stubRunner(QualityAgentType.API_CHANGE_DETECTION,
                AnalysisResult.builder().riskLevel(RiskLevel.LOW).build());

        @Config.LoadPolicy(Config.LoadType.MERGE)
        @Config.Sources({
                "classpath:config/quality-intelligence-test.properties",
                "system:properties"
        })
        interface ContinueOnFailureConfig extends QualityIntelligenceConfig {
            @Override
            @Config.Key("agent.quality.continue.on.failure")
            @Config.DefaultValue("true")
            boolean continueOnFailure();
        }

        QualityIntelligenceResult result = new QualityIntelligenceOrchestrator(
                ConfigFactory.create(ContinueOnFailureConfig.class),
                List.of(failing, ok)).run();

        assertThat(result.getAgentsFailed()).isEqualTo(1);
        assertThat(result.getAgentsSucceeded()).isEqualTo(1);
    }

    private static QualityAgentResultsBundle sampleBundle() {
        QualityAgentResultsBundle bundle = new QualityAgentResultsBundle();
        bundle.add(QualityAgentRunResult.success(QualityAgentType.API_CHANGE_DETECTION,
                AnalysisResult.builder().riskLevel(RiskLevel.MEDIUM).build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.TEST_GAP_ANALYZER,
                TestGapAnalysisResult.builder().overallCoveragePercent(75.0).build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.RELEASE_RISK_ASSESSMENT,
                ReleaseRiskAssessmentResult.builder()
                        .releaseRiskScore(30.0)
                        .riskCategory(ReleaseRiskCategory.YELLOW)
                        .recommendation(ReleaseRecommendation.APPROVE_WITH_RISK)
                        .build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.ARCHITECTURE_DRIFT,
                ArchitectureDriftResult.builder().architectureHealthScore(70).issuesFound(3).build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.FLAKY_TEST_INVESTIGATOR,
                FlakyInvestigationResult.builder().portfolioFlakinessPercent(10.0).flakyTestCount(2).build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.RISK_BASED_REGRESSION,
                RiskBasedRegressionResult.builder()
                        .recommendation(RegressionScopeRecommendation.PARTIAL_REGRESSION)
                        .modulesAnalyzed(2)
                        .build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.REQUIREMENTS_TRACEABILITY,
                TraceabilityAnalysisResult.builder().overallCoveragePercent(68.0).issues(List.of()).build(), 10));
        bundle.add(QualityAgentRunResult.success(QualityAgentType.TEST_REVIEW,
                TestReviewResult.builder().overallVerdict(ReviewVerdict.APPROVED_WITH_RISK).build(), 10));
        return bundle;
    }

    private static QualityAgentRunner stubRunner(QualityAgentType type, Object payload) {
        return new AbstractQualityAgentRunner(type) {
            @Override
            protected Object execute() {
                return payload;
            }
        };
    }
}
