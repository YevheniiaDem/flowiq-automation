package com.flowiq.agents.factory.orchestrator;

import com.flowiq.agents.factory.config.PrIntelligenceConfig;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.PrIntelligenceResult;
import com.flowiq.agents.factory.model.PrIntelligenceVerdict;
import com.flowiq.agents.factory.report.PrIntelligenceReportGenerator;
import com.flowiq.agents.factory.runner.AbstractFactoryAgentRunner;
import com.flowiq.agents.factory.runner.FactoryAgentRunner;
import com.flowiq.agents.factory.scorer.PrIntelligenceScorer;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.prreview.model.PrReviewVerdict;
import com.flowiq.agents.prreview.model.PullRequestReviewResult;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrIntelligenceOrchestratorTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/pr-intelligence-test.properties")
    interface TestPrIntelligenceConfig extends PrIntelligenceConfig {
    }

    private static PrIntelligenceConfig testConfig() {
        return ConfigFactory.create(TestPrIntelligenceConfig.class);
    }

    @Test(groups = "unit")
    public void prIntelligenceVerdictShouldMapScoreRanges() {
        assertThat(PrIntelligenceVerdict.worst(
                PrIntelligenceVerdict.APPROVED, PrIntelligenceVerdict.REJECTED))
                .isEqualTo(PrIntelligenceVerdict.REJECTED);
    }

    @Test(groups = "unit")
    public void prIntelligenceScorerShouldPenalizeRejectedReviews() {
        var bundle = new com.flowiq.agents.factory.model.FactoryAgentResultsBundle();
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.PULL_REQUEST_REVIEW,
                PullRequestReviewResult.builder().verdict(PrReviewVerdict.REJECTED).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.TEST_REVIEW,
                TestReviewResult.builder().overallVerdict(ReviewVerdict.REJECTED).rejectedCount(2).build(), 1));
        bundle.add(com.flowiq.agents.factory.model.FactoryAgentRunResult.success(
                FactoryAgentType.API_CHANGE_DETECTION,
                AnalysisResult.builder().riskLevel(RiskLevel.HIGH).build(), 1));

        PrIntelligenceScorer scorer = new PrIntelligenceScorer();
        int score = scorer.calculateScore(bundle);
        PrIntelligenceVerdict verdict = scorer.determineVerdict(bundle, score);

        assertThat(score).isLessThan(60);
        assertThat(verdict).isEqualTo(PrIntelligenceVerdict.REJECTED);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        PrIntelligenceResult result = PrIntelligenceResult.builder()
                .analyzedAt(Instant.parse("2026-06-18T12:00:00Z"))
                .prQualityScore(82)
                .verdict(PrIntelligenceVerdict.APPROVED_WITH_RISK)
                .agentsSucceeded(4)
                .agentsFailed(0)
                .summaryLine("PR Quality Score: 82/100.")
                .riskLine("Full regression scope required")
                .actionLine("Review API change report")
                .build();

        var path = new PrIntelligenceReportGenerator(testConfig()).generate(result);
        String content = Files.readString(path);

        assertThat(content).contains("# PR Intelligence Report");
        assertThat(content).contains("## PR Quality Score");
        assertThat(content).contains("APPROVED WITH RISK");
        assertThat(content).contains("## Top Risks");
        assertThat(content).contains("## Recommended Actions");
    }

    @Test(groups = "unit")
    public void orchestratorShouldRunStubAgentsAndProduceScore() {
        List<FactoryAgentRunner> runners = List.of(
                stubRunner(FactoryAgentType.PULL_REQUEST_REVIEW,
                        PullRequestReviewResult.builder().verdict(PrReviewVerdict.APPROVED).build()),
                stubRunner(FactoryAgentType.TEST_REVIEW,
                        TestReviewResult.builder().overallVerdict(ReviewVerdict.APPROVED).build()),
                stubRunner(FactoryAgentType.API_CHANGE_DETECTION,
                        AnalysisResult.builder().riskLevel(RiskLevel.LOW).build()),
                stubRunner(FactoryAgentType.RISK_BASED_REGRESSION,
                        RiskBasedRegressionResult.builder()
                                .recommendation(RegressionScopeRecommendation.SMOKE_ONLY)
                                .build()));

        PrIntelligenceResult result = new PrIntelligenceOrchestrator(testConfig(), runners).run();

        assertThat(result.getPrQualityScore()).isBetween(70, 100);
        assertThat(result.getVerdict()).isIn(PrIntelligenceVerdict.APPROVED, PrIntelligenceVerdict.APPROVED_WITH_RISK);
        assertThat(result.getAgentsSucceeded()).isEqualTo(4);
        assertThat(result.getAgentsFailed()).isZero();
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
