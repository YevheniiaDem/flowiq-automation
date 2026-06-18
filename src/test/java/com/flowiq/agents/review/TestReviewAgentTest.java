package com.flowiq.agents.review;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import com.flowiq.agents.review.analyzer.CoverageImpactAnalyzer;
import com.flowiq.agents.review.analyzer.MissingTestAnalyzer;
import com.flowiq.agents.review.config.TestReviewAgentConfig;
import com.flowiq.agents.review.model.CoverageStatus;
import com.flowiq.agents.review.model.FeatureChange;
import com.flowiq.agents.review.model.FeatureChangeType;
import com.flowiq.agents.review.model.FeatureReviewItem;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import com.flowiq.agents.review.report.ReviewCommentGenerator;
import com.flowiq.agents.review.scanner.FeatureChangeScanner;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReviewAgentTest {

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/test-review-test.properties")
    interface TestReviewConfig extends TestReviewAgentConfig {
    }

    private static TestReviewAgentConfig testConfig() {
        return ConfigFactory.create(TestReviewConfig.class);
    }

    @Test(groups = "unit")
    public void featureChangeScannerShouldDetectControllerServiceAndDto() {
        TestReviewAgentConfig config = testConfig();
        List<String> changedFiles = new FeatureChangeScanner(config).loadChangedFiles();

        List<FeatureChange> features = new FeatureChangeScanner(config).scan(changedFiles, List.of());

        assertThat(changedFiles).hasSize(3);
        assertThat(features).extracting(FeatureChange::getChangeType)
                .contains(FeatureChangeType.CONTROLLER, FeatureChangeType.SERVICE, FeatureChangeType.DTO);
        assertThat(features).allMatch(f -> "imports".equals(f.getModule()));
    }

    @Test(groups = "unit")
    public void featureChangeScannerShouldMapNewApiEndpoints() {
        TestReviewAgentConfig config = testConfig();
        List<ApiChange> apiChanges = List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload",
                        "New CSV upload endpoint"),
                ApiChange.schema(ChangeType.MODIFIED_REQUEST_SCHEMA, "ImportUploadRequest",
                        "Added required field fileName", false));

        List<FeatureChange> features = new FeatureChangeScanner(config).scan(List.of(), apiChanges);

        assertThat(features).anyMatch(f -> f.getChangeType() == FeatureChangeType.ENDPOINT
                && "POST".equals(f.getHttpMethod())
                && "/imports/upload".equals(f.getEndpointPath()));
        assertThat(features).anyMatch(f -> f.getChangeType() == FeatureChangeType.DTO
                && "ImportUploadRequest".equals(f.getSchemaName()));
    }

    @Test(groups = "unit")
    public void coverageImpactAnalyzerShouldReflectImportsModuleSuites() {
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        List<ScannedTestReference> tests = new TestSourceScanner(gapConfig).scan();
        CoverageImpactAnalyzer analyzer = new CoverageImpactAnalyzer(new BusinessImpactPrioritizer(gapConfig));

        FeatureChange endpoint = FeatureChange.builder()
                .featureId("imports-post-upload")
                .featureName("POST /imports/upload")
                .changeType(FeatureChangeType.ENDPOINT)
                .module("imports")
                .httpMethod("POST")
                .endpointPath("/imports/upload")
                .description("New upload endpoint")
                .build();

        CoverageStatus coverage = analyzer.analyze(endpoint, tests);

        assertThat(coverage.isRegressionCovered()).isTrue();
        assertThat(coverage.isUiCovered()).isTrue();
        assertThat(coverage.isContractCovered()).isFalse();
    }

    @Test(groups = "unit")
    public void missingTestAnalyzerShouldRejectNewEndpointWithoutContract() {
        TestReviewAgentConfig config = testConfig();
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        BusinessImpactPrioritizer prioritizer = new BusinessImpactPrioritizer(gapConfig);
        CoverageImpactAnalyzer coverageAnalyzer = new CoverageImpactAnalyzer(prioritizer);
        MissingTestAnalyzer analyzer = new MissingTestAnalyzer(config, prioritizer, coverageAnalyzer);

        FeatureChange feature = FeatureChange.builder()
                .featureId("imports-post-upload")
                .featureName("POST /imports/upload")
                .changeType(FeatureChangeType.ENDPOINT)
                .module("imports")
                .httpMethod("POST")
                .endpointPath("/imports/upload")
                .description("New upload endpoint")
                .build();

        CoverageStatus coverage = CoverageStatus.builder()
                .smokeCovered(true)
                .regressionCovered(true)
                .contractCovered(false)
                .uiCovered(true)
                .positiveCovered(true)
                .negativeCovered(false)
                .authorizationCovered(false)
                .build();

        FeatureReviewItem review = analyzer.analyze(feature, coverage);

        assertThat(review.getVerdict()).isEqualTo(ReviewVerdict.REJECTED);
        assertThat(review.getMissingTests()).contains("Contract test coverage");
        assertThat(review.getRecommendation()).contains("contract tests");
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        TestReviewAgentConfig config = testConfig();
        FeatureChange feature = FeatureChange.builder()
                .featureId("imports-post-upload")
                .featureName("POST /imports/upload")
                .changeType(FeatureChangeType.ENDPOINT)
                .module("imports")
                .httpMethod("POST")
                .endpointPath("/imports/upload")
                .changedFile("src/main/java/com/flowiq/imports/ImportsController.java")
                .description("New upload endpoint")
                .build();

        TestReviewResult result = TestReviewResult.builder()
                .reviewedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .overallVerdict(ReviewVerdict.REJECTED)
                .featuresReviewed(1)
                .rejectedCount(1)
                .approvedWithRiskCount(0)
                .feature(FeatureReviewItem.builder()
                        .feature(feature)
                        .coverageStatus(CoverageStatus.builder()
                                .smokeCovered(true)
                                .regressionCovered(true)
                                .contractCovered(false)
                                .uiCovered(true)
                                .positiveCovered(true)
                                .negativeCovered(false)
                                .authorizationCovered(false)
                                .build())
                        .missingTest("Contract test coverage")
                        .risk(com.flowiq.agents.gap.model.GapSeverity.HIGH)
                        .recommendation("Add contract tests before merge.")
                        .verdict(ReviewVerdict.REJECTED)
                        .build())
                .summaryLine("Overall PR test review verdict: REJECTED.")
                .dataSourcesSummary("Changed files: 3; API changes: 1")
                .pullRequestSummary("3 changed file(s), 1 API change(s) detected.")
                .build();

        Path reportPath = new ReviewCommentGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Pull Request Test Review");
        assertThat(content).contains("## Overall Verdict");
        assertThat(content).contains("REJECTED");
        assertThat(content).contains("**Feature**");
        assertThat(content).contains("**Changed Files**");
        assertThat(content).contains("**Coverage Status**");
        assertThat(content).contains("**Missing Tests**");
        assertThat(content).contains("**Risk**");
        assertThat(content).contains("**Recommendation**");
    }

    @Test(groups = "unit")
    public void agentShouldRunWithManifestAndApiChanges() {
        TestReviewAgentConfig config = testConfig();
        List<String> changedFiles = new FeatureChangeScanner(config).loadChangedFiles();
        List<ApiChange> apiChanges = List.of(
                ApiChange.endpoint(ChangeType.ADDED_ENDPOINT, "POST", "/imports/upload",
                        "New CSV upload endpoint"));

        TestReviewResult result = new TestReviewAgent(config)
                .run(changedFiles, apiChanges, "unit-test fixtures");

        assertThat(result.getFeaturesReviewed()).isGreaterThan(0);
        assertThat(result.getOverallVerdict()).isIn(
                ReviewVerdict.REJECTED, ReviewVerdict.APPROVED_WITH_RISK, ReviewVerdict.APPROVED);
        assertThat(result.getFeatures()).isNotEmpty();
        assertThat(result.getFeatures().stream().map(FeatureReviewItem::getVerdict))
                .contains(ReviewVerdict.REJECTED);
        assertThat(result.getRejectedCount()).isGreaterThan(0);
    }
}
