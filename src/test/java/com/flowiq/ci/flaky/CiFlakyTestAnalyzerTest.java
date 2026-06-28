package com.flowiq.ci.flaky;

import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.ci.flaky.analyzer.DurationStabilityAnalyzer;
import com.flowiq.ci.flaky.filter.BusinessTestExecutionFilter;
import com.flowiq.ci.flaky.history.FlakyHistoryStore;
import com.flowiq.ci.flaky.model.CiFlakyReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CiFlakyTestAnalyzerTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test(groups = "unit")
  public void durationAnalyzerShouldDetectUnstableRuns() {
    List<TestExecutionRecord> runs = List.of(
        record("t1", 1000),
        record("t1", 5000),
        record("t1", 1200),
        record("t1", 4800));
    DurationStabilityAnalyzer.DurationMetrics metrics = new DurationStabilityAnalyzer(0.35, 3).analyze(runs);
    assertThat(metrics.isUnstable()).isTrue();
  }

  @Test(groups = "unit")
  public void businessFilterShouldExcludeInfrastructureSignals() {
    TestExecutionRecord infra = TestExecutionRecord.builder()
        .testKey("ci-up.sh")
        .className("docker.compose")
        .methodName("retry")
        .build();
    TestExecutionRecord business = TestExecutionRecord.builder()
        .testKey("com.flowiq.api.AuthTest#login")
        .className("com.flowiq.api.AuthTest")
        .methodName("login")
        .build();
    assertThat(BusinessTestExecutionFilter.isBusinessTest(infra)).isFalse();
    assertThat(BusinessTestExecutionFilter.isBusinessTest(business)).isTrue();
  }

  @Test(groups = "unit")
  public void analyzerShouldSeparateFailedFromFlaky() throws Exception {
    Path temp = Files.createTempDirectory("flaky-ci-test");
    Path allureDir = temp.resolve("allure");
    Path historyFile = temp.resolve("history.json");
    Path outputDir = temp.resolve("out");
    Files.createDirectories(allureDir);

    copyFixture("allure-run1", allureDir);
    new CiFlakyTestAnalyzer(2, 10).analyze(new CiFlakyTestAnalyzer.CiFlakyAnalysisRequest(
        allureDir, historyFile, outputDir, temp.resolve("summary.md"),
        "run-1", "test"));

    Files.list(allureDir).forEach(path -> {
      try {
        Files.deleteIfExists(path);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    copyFixture("allure-run2", allureDir);
    CiFlakyReport report = new CiFlakyTestAnalyzer(2, 10).analyze(
        new CiFlakyTestAnalyzer.CiFlakyAnalysisRequest(
            allureDir, historyFile, outputDir, temp.resolve("summary.md"),
            "run-2", "test"));

    assertThat(report.getFlakyCount()).isGreaterThan(0);
    assertThat(Files.exists(outputDir.resolve("flaky-report.json"))).isTrue();
    assertThat(Files.exists(outputDir.resolve("flaky-report.html"))).isTrue();
    assertThat(Files.exists(historyFile)).isTrue();
  }

  private static TestExecutionRecord record(String key, long durationMs) {
    return TestExecutionRecord.builder()
        .testKey(key)
        .className("com.example.Test")
        .methodName("method")
        .durationMs(durationMs)
        .build();
  }

  private static void copyFixture(String fixture, Path targetDir) throws Exception {
    Path source = Path.of("src/test/resources/agents/flaky", fixture);
    Files.createDirectories(targetDir);
    try (var files = Files.walk(source)) {
      files.filter(Files::isRegularFile).forEach(path -> {
        try {
          Files.copy(path, targetDir.resolve(path.getFileName().toString()),
              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
