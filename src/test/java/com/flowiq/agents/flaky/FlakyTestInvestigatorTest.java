package com.flowiq.agents.flaky;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.aggregator.StabilityMetricsCalculator;
import com.flowiq.agents.flaky.analyzer.RootCauseAnalyzerPipeline;
import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.flaky.model.RootCauseType;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestStabilityMetrics;
import com.flowiq.agents.flaky.report.FlakyTestReportGenerator;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FlakyTestInvestigatorTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test(groups = "unit")
  public void flakinessCalculatorShouldDetectOscillatingTests() {
    double flaky = StabilityMetricsCalculator.calculateFlakiness(3, 3);
    assertThat(flaky).isEqualTo(50.0);
    assertThat(StabilityMetricsCalculator.calculateFlakiness(5, 0)).isZero();
  }

  @Test(groups = "unit")
  public void allureLoaderShouldParseFixtureResults() {
    Path run1 = fixturePath("allure-run1");
    Path run2 = fixturePath("allure-run2");
    List<TestExecutionRecord> records = new ArrayList<>();
    records.addAll(new AllureResultsLoader("allure", run1, MAPPER).load());
    records.addAll(new AllureResultsLoader("allure", run2, MAPPER).load());

    assertThat(records).hasSize(4);
    assertThat(records.stream().map(TestExecutionRecord::getTestKey).distinct()).hasSize(2);
  }

  @Test(groups = "unit")
  public void rootCausePipelineShouldDetectTimeoutAndLocator() {
    var pipeline = new RootCauseAnalyzerPipeline();
    List<TestExecutionRecord> timeoutFailure = List.of(
        TestExecutionRecord.builder()
            .testKey("com.flowiq.ui.smoke.AIAccountantSmokeTest#shouldSendMessage")
            .message("Timeout 30000ms exceeded waiting for locator")
            .stackTrace("playwright.TimeoutError")
            .build());
    assertThat(pipeline.analyze(timeoutFailure).primary().getType()).isEqualTo(RootCauseType.TIMEOUT);

    List<TestExecutionRecord> locatorFailure = List.of(
        TestExecutionRecord.builder()
            .testKey("com.flowiq.ui.smoke.ForecastsSmokeTest#shouldShowWarnings")
            .message("Element not found: strict mode violation")
            .stackTrace("locator resolved to 0 elements")
            .build());
    assertThat(pipeline.analyze(locatorFailure).primary().getType()).isEqualTo(RootCauseType.LOCATOR_ISSUE);
  }

  @Test(groups = "unit")
  public void investigatorShouldIdentifyFlakyTestsFromFixtures() {
    List<TestExecutionRecord> records = loadFixtureRecords();
    FlakyInvestigationResult result = new FlakyTestInvestigator()
        .run(records, "test-fixtures");

    assertThat(result.getFlakyTestCount()).isEqualTo(2);
    assertThat(result.getTopUnstableTests()).isNotEmpty();
    assertThat(result.getTopUnstableTests().get(0).getMetrics().getFlakinessPercent()).isEqualTo(50.0);
    assertThat(result.getPortfolioPassRate()).isEqualTo(50.0);
  }

  @Test(groups = "unit")
  public void reportShouldContainLeadershipSections() throws Exception {
    List<TestExecutionRecord> records = loadFixtureRecords();
    FlakyInvestigationResult result = new FlakyTestInvestigator().run(records, "test-fixtures");
    var config = ConfigFactory.create(com.flowiq.agents.flaky.config.FlakyTestAgentConfig.class);
    Path report = new FlakyTestReportGenerator(config).generate(result);
    String content = Files.readString(report);

    assertThat(content).contains("# Flaky Test Investigation Report");
    assertThat(content).contains("## Executive Summary");
    assertThat(content).contains("## Portfolio Metrics");
    assertThat(content).contains("Pass rate");
    assertThat(content).contains("## Top ");
    assertThat(content).contains("Root Cause Hypotheses");
    assertThat(content).contains("Recommended Actions for Leadership");
  }

  private static List<TestExecutionRecord> loadFixtureRecords() {
    Path run1 = fixturePath("allure-run1");
    Path run2 = fixturePath("allure-run2");
    List<TestExecutionRecord> records = new ArrayList<>();
    records.addAll(new AllureResultsLoader("allure", run1, MAPPER).load());
    records.addAll(new AllureResultsLoader("allure", run2, MAPPER).load());
    return records;
  }

  private static Path fixturePath(String name) {
    return Path.of("src/test/resources/agents/flaky", name).toAbsolutePath().normalize();
  }
}
