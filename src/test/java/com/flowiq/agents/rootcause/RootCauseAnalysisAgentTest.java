package com.flowiq.agents.rootcause;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.rootcause.analyzer.RootCauseAnalyzerPipeline;
import com.flowiq.agents.rootcause.config.RootCauseAgentConfig;
import com.flowiq.agents.rootcause.loader.FailureArtifactAggregator;
import com.flowiq.agents.rootcause.model.FailedTestContext;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.rootcause.model.RootCauseCategory;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import com.flowiq.agents.rootcause.report.RootCauseAnalysisReportGenerator;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RootCauseAnalysisAgentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Config.LoadPolicy(Config.LoadType.MERGE)
    @Config.Sources("classpath:config/root-cause-analysis-test.properties")
    interface TestRootCauseConfig extends RootCauseAgentConfig {
    }

    private static RootCauseAgentConfig testConfig() {
        return ConfigFactory.create(TestRootCauseConfig.class);
    }

    @Test(groups = "unit")
    public void allureLoaderShouldParseRootCauseFixtures() {
        Path allureDir = fixturePath("allure");
        List<TestExecutionRecord> records = new AllureResultsLoader("allure", allureDir, MAPPER).load();

        assertThat(records).hasSizeGreaterThanOrEqualTo(4);
        assertThat(records.stream().map(TestExecutionRecord::getTestKey).distinct()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test(groups = "unit")
    public void pipelineShouldDetectUiFailure() {
        FailedTestContext context = FailedTestContext.builder()
                .execution(TestExecutionRecord.builder()
                        .testKey("com.flowiq.ui.smoke.ForecastsSmokeTest#shouldShowWarnings")
                        .className("com.flowiq.ui.smoke.ForecastsSmokeTest")
                        .methodName("shouldShowWarnings")
                        .suite("ui")
                        .message("strict mode violation waiting for locator(\".forecast-warning\")")
                        .stackTrace("playwright.TimeoutError: locator resolved to 0 elements")
                        .build())
                .build();

        RootCauseFinding finding = new RootCauseAnalyzerPipeline().analyze(context);

        assertThat(finding.getMostProbableRootCause()).isEqualTo(RootCauseCategory.UI_BUG);
        assertThat(finding.getConfidence()).isGreaterThanOrEqualTo(70);
    }

    @Test(groups = "unit")
    public void pipelineShouldDetectBackendFailureWithLogs() {
        FailedTestContext context = FailedTestContext.builder()
                .execution(TestExecutionRecord.builder()
                        .testKey("com.flowiq.api.regression.tasks.TasksRegressionTest#shouldCreateTask")
                        .className("com.flowiq.api.regression.tasks.TasksRegressionTest")
                        .methodName("shouldCreateTask")
                        .suite("regression")
                        .message("Expected status code 201 but was 500 Internal Server Error")
                        .stackTrace("AssertionError at TasksRegressionTest.java:48")
                        .build())
                .backendLogLine("ERROR TasksController - Unhandled exception status 500 Internal Server Error")
                .build();

        RootCauseFinding finding = new RootCauseAnalyzerPipeline().analyze(context);

        assertThat(finding.getMostProbableRootCause()).isEqualTo(RootCauseCategory.BACKEND_BUG);
        assertThat(finding.getConfidence()).isGreaterThanOrEqualTo(80);
        assertThat(finding.getEvidence()).isNotEmpty();
    }

    @Test(groups = "unit")
    public void pipelineShouldDetectAuthFailure() {
        FailedTestContext context = FailedTestContext.builder()
                .execution(TestExecutionRecord.builder()
                        .testKey("com.flowiq.api.regression.auth.AuthRegressionTest#shouldRejectExpiredToken")
                        .className("com.flowiq.api.regression.auth.AuthRegressionTest")
                        .methodName("shouldRejectExpiredToken")
                        .message("Expected 401 Unauthorized but was 403 Forbidden — JWT expired")
                        .build())
                .build();

        RootCauseFinding finding = new RootCauseAnalyzerPipeline().analyze(context);

        assertThat(finding.getMostProbableRootCause()).isEqualTo(RootCauseCategory.AUTH);
        assertThat(finding.getConfidence()).isGreaterThanOrEqualTo(70);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        RootCauseAgentConfig config = testConfig();
        RootCauseAnalysisResult result = RootCauseAnalysisResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .failuresAnalyzed(1)
                .highConfidenceFindings(1)
                .finding(RootCauseFinding.builder()
                        .failedTest("com.flowiq.ui.smoke.ForecastsSmokeTest#shouldShowWarnings")
                        .symptoms("ui suite failure: strict mode violation")
                        .mostProbableRootCause(RootCauseCategory.UI_BUG)
                        .confidence(92)
                        .evidenceItem("Matched Playwright locator failure")
                        .recommendedFix("Stabilize locator with data-testid.")
                        .build())
                .summaryLine("1 failed test(s) analyzed.")
                .dataSourcesSummary("test-fixtures")
                .build();

        Path reportPath = new RootCauseAnalysisReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Root Cause Analysis");
        assertThat(content).contains("**Failed Test**");
        assertThat(content).contains("**Symptoms**");
        assertThat(content).contains("**Most Probable Root Cause**");
        assertThat(content).contains("**Confidence**");
        assertThat(content).contains("**Evidence**");
        assertThat(content).contains("**Recommended Fix**");
        assertThat(content).contains("UI_BUG");
    }

    @Test(groups = "unit")
    public void agentShouldAnalyzeFailuresFromFixtures() {
        RootCauseAgentConfig config = testConfig();
        List<FailedTestContext> failures = new FailureArtifactAggregator(config, MAPPER).loadFailedTests();

        RootCauseAnalysisResult result = new RootCauseAnalysisAgent(config)
                .run(failures, "root-cause test fixtures");

        assertThat(failures).isNotEmpty();
        assertThat(result.getFailuresAnalyzed()).isEqualTo(failures.size());
        assertThat(result.getFindings()).hasSize(failures.size());
        assertThat(result.getFindings().stream().map(RootCauseFinding::getMostProbableRootCause))
                .contains(RootCauseCategory.UI_BUG, RootCauseCategory.BACKEND_BUG, RootCauseCategory.AUTH);
        assertThat(result.getHighConfidenceFindings()).isGreaterThan(0);
    }

    private static Path fixturePath(String name) {
        return Path.of("src/test/resources/agents/rootcause", name).toAbsolutePath().normalize();
    }
}
