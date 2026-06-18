package com.flowiq.agents.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.loader.SurefireReportLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.release.analyzer.BlockedAreaAnalyzer;
import com.flowiq.agents.release.analyzer.CriticalFailureAnalyzer;
import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.loader.ReleaseTestResultsLoader;
import com.flowiq.agents.release.model.ApiChangeReportInsight;
import com.flowiq.agents.release.model.FlakyReportInsight;
import com.flowiq.agents.release.model.ReleaseRecommendation;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import com.flowiq.agents.release.model.SuiteExecutionSummary;
import com.flowiq.agents.release.parser.ApiChangeReportParser;
import com.flowiq.agents.release.parser.FlakyReportParser;
import com.flowiq.agents.release.report.ReleaseReadinessReportGenerator;
import com.flowiq.agents.release.scorer.ReleaseRiskScoreCalculator;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReleaseRiskAssessmentAgentTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @LoadPolicy(LoadType.MERGE)
  @Sources("classpath:config/release-risk-assessment-test.properties")
  interface TestReleaseRiskAgentConfig extends ReleaseRiskAgentConfig {
  }

  private static ReleaseRiskAgentConfig testConfig() {
    return ConfigFactory.create(TestReleaseRiskAgentConfig.class);
  }

  @Test(groups = "unit")
  public void surefireLoaderShouldParseReleaseFixtures() {
    Path dir = fixturePath("surefire");
    List<TestExecutionRecord> records = new SurefireReportLoader("fixture", dir).load();

    assertThat(records).hasSizeGreaterThanOrEqualTo(5);
    assertThat(records.stream().anyMatch(r -> r.getClassName().contains("Smoke"))).isTrue();
    assertThat(records.stream().anyMatch(r -> r.getClassName().contains("Regression"))).isTrue();
    assertThat(records.stream().anyMatch(r -> r.getClassName().contains("Contract"))).isTrue();
  }

  @Test(groups = "unit")
  public void suiteSummariesShouldComputePassRates() {
    ReleaseRiskAgentConfig config = testConfig();
    List<TestExecutionRecord> records = loadFixtureRecords();
    Map<TestSuiteType, SuiteExecutionSummary> summaries =
        new ReleaseTestResultsLoader(config, MAPPER).summarize(records, "fixtures");

    assertThat(summaries.get(TestSuiteType.SMOKE).getPassRate()).isEqualTo(50.0);
    assertThat(summaries.get(TestSuiteType.CONTRACT).getPassRate()).isEqualTo(100.0);
    assertThat(summaries.get(TestSuiteType.REGRESSION).getPassRate())
            .isEqualTo(200.0 / 3.0, org.assertj.core.data.Offset.offset(0.1));
  }

  @Test(groups = "unit")
  public void flakyReportParserShouldExtractMetrics() throws Exception {
    String content = readFixtureText("agents/release/flaky-report.md");
    FlakyReportInsight insight = new FlakyReportParser(testConfig()).parseContent(content);

    assertThat(insight.isReportFound()).isTrue();
    assertThat(insight.getFlakyTestCount()).isEqualTo(2);
    assertThat(insight.getPortfolioPassRate()).isEqualTo(92.0);
    assertThat(insight.getTopUnstableTests()).contains("shouldSendMessage");
  }

  @Test(groups = "unit")
  public void apiChangeReportParserShouldDetectBreakingChanges() throws Exception {
    String content = readFixtureText("agents/release/api-change-report.md");
    ApiChangeReportInsight insight = new ApiChangeReportParser(testConfig()).parseContent(content);

    assertThat(insight.isReportFound()).isTrue();
    assertThat(insight.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
    assertThat(insight.getBreakingChanges()).isEqualTo(2);
    assertThat(insight.getAffectedContractTests()).contains("AuthContractTest");
  }

  @Test(groups = "unit")
  public void scoreCalculatorShouldFlagSmokeFailuresAsHighRisk() {
    ReleaseRiskAgentConfig config = testConfig();
    List<TestExecutionRecord> records = loadFixtureRecords();
    ReleaseTestResultsLoader loader = new ReleaseTestResultsLoader(config, MAPPER);
    Map<TestSuiteType, SuiteExecutionSummary> summaries = loader.summarize(records, "fixtures");

    var critical = new CriticalFailureAnalyzer().analyze(records);
    var flaky = new FlakyReportParser(config).parseContent(readFixtureTextSafe("agents/release/flaky-report.md"));
    var api = new ApiChangeReportParser(config).parseContent(readFixtureTextSafe("agents/release/api-change-report.md"));

    var score = new ReleaseRiskScoreCalculator(config).calculate(
            summaries.get(TestSuiteType.REGRESSION),
            summaries.get(TestSuiteType.SMOKE),
            summaries.get(TestSuiteType.CONTRACT),
            flaky, api, critical);

    assertThat(score.score()).isGreaterThan(config.greenScoreMax());
    assertThat(score.recommendation()).isEqualTo(ReleaseRecommendation.DO_NOT_RELEASE);
  }

  @Test(groups = "unit")
  public void reportShouldContainRequiredSections() throws Exception {
    ReleaseRiskAgentConfig config = testConfig();
    var result = ReleaseRiskAssessmentResult.builder()
            .assessedAt(Instant.parse("2026-06-17T12:00:00Z"))
            .releaseRiskScore(45.0)
            .riskCategory(ReleaseRiskCategory.YELLOW)
            .recommendation(ReleaseRecommendation.APPROVE_WITH_RISK)
            .regressionSummary(SuiteExecutionSummary.builder()
                    .suiteType(TestSuiteType.REGRESSION).totalTests(10).passed(9).failed(1)
                    .passRate(90.0).build())
            .smokeSummary(SuiteExecutionSummary.builder()
                    .suiteType(TestSuiteType.SMOKE).totalTests(5).passed(5)
                    .passRate(100.0).build())
            .contractSummary(SuiteExecutionSummary.builder()
                    .suiteType(TestSuiteType.CONTRACT).totalTests(4).passed(4)
                    .passRate(100.0).build())
            .flakyInsight(FlakyReportInsight.builder().reportFound(true).flakyTestCount(1).summary("1 flaky").build())
            .apiChangeInsight(ApiChangeReportInsight.builder().reportFound(true).riskLevel(RiskLevel.LOW).summary("ok").build())
            .recommendedAction("Re-run smoke suite")
            .summaryLine("Test summary")
            .scoreBreakdown(Map.of("smoke", 5.0, "regression", 10.0))
            .dataSourcesSummary("fixtures")
            .build();

    Path report = new ReleaseReadinessReportGenerator(config).generate(result);
    String content = Files.readString(report);

    assertThat(content).contains("# Release Readiness Report");
    assertThat(content).contains("## Overall Score");
    assertThat(content).contains("## Critical Failures");
    assertThat(content).contains("## Blocked Areas");
    assertThat(content).contains("## Recommended Actions");
    assertThat(content).contains("## Final Recommendation");
    assertThat(content).contains("APPROVE WITH RISK");
  }

  @Test(groups = "unit")
  public void agentShouldAssessReleaseFromFixtures() throws Exception {
    ReleaseRiskAgentConfig config = testConfig();
    List<TestExecutionRecord> records = loadFixtureRecords();
    FlakyReportInsight flaky = new FlakyReportParser(config)
            .parseContent(readFixtureText("agents/release/flaky-report.md"));
    ApiChangeReportInsight api = new ApiChangeReportParser(config)
            .parseContent(readFixtureText("agents/release/api-change-report.md"));

    ReleaseRiskAssessmentResult result = new ReleaseRiskAssessmentAgent(config)
            .run(records, "release-fixtures", flaky, api);

    assertThat(result.getReleaseRiskScore()).isGreaterThan(0.0);
    assertThat(result.getRiskCategory()).isIn(ReleaseRiskCategory.YELLOW, ReleaseRiskCategory.RED);
    assertThat(result.getRecommendation()).isEqualTo(ReleaseRecommendation.DO_NOT_RELEASE);
    assertThat(result.getCriticalFailures()).isNotEmpty();
    assertThat(result.getBlockedAreas()).isNotEmpty();
    assertThat(new BlockedAreaAnalyzer().analyze(result.getCriticalFailures()))
            .anyMatch(a -> "auth".equals(a.getModule()));
    assertThat(result.getRecommendedActions()).isNotEmpty();
  }

  private static List<TestExecutionRecord> loadFixtureRecords() {
    return new SurefireReportLoader("fixture", fixturePath("surefire")).load();
  }

  private static Path fixturePath(String name) {
    return Path.of("src/test/resources/agents/release", name).toAbsolutePath().normalize();
  }

  private static String readFixtureText(String resource) throws IOException {
    try (InputStream input = ReleaseRiskAssessmentAgentTest.class.getClassLoader().getResourceAsStream(resource)) {
      assertThat(input).as("fixture %s", resource).isNotNull();
      return new String(input.readAllBytes());
    }
  }

  private static String readFixtureTextSafe(String resource) {
    try {
      return readFixtureText(resource);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
