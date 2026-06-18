package com.flowiq.agents.selfhealing;

import com.flowiq.agents.selfhealing.analyzer.LocatorFailureAnalyzer;
import com.flowiq.agents.selfhealing.collector.DomSnapshotCollector;
import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.engine.LocatorSimilarityEngine;
import com.flowiq.agents.selfhealing.generator.SuggestedLocatorGenerator;
import com.flowiq.agents.selfhealing.llm.NoOpSelfHealingLlmProvider;
import com.flowiq.agents.selfhealing.model.LocatorConfidence;
import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorRisk;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import com.flowiq.agents.selfhealing.model.LocatorType;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;
import com.flowiq.agents.selfhealing.report.SelfHealingReportGenerator;
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

import static org.assertj.core.api.Assertions.assertThat;

public class SelfHealingLocatorAgentTest {

  @LoadPolicy(LoadType.MERGE)
  @Sources("classpath:config/self-healing-test.properties")
  interface TestSelfHealingConfig extends SelfHealingAgentConfig {
  }

  private static SelfHealingAgentConfig testConfig() {
    return ConfigFactory.create(TestSelfHealingConfig.class);
  }

  @Test(groups = "unit")
  public void levenshteinEngineShouldComputeSimilarity() {
    assertThat(LocatorSimilarityEngine.similarity("send-btn", "sendbtn")).isEqualTo(1.0);
    assertThat(LocatorSimilarityEngine.similarity("old-send-btn", "ai-accountant-chat-send-btn"))
            .isGreaterThan(0.3);
    assertThat(LocatorSimilarityEngine.levenshteinDistance("kitten", "sitting")).isEqualTo(3);
  }

  @Test(groups = "unit")
  public void domSnapshotCollectorShouldParseTestIdElements() throws Exception {
    String html = readFixtureText("agents/selfhealing/dom/AIAccountantSmokeTest_shouldSendMessage.html");
    var elements = new DomSnapshotCollector().parseHtml(html);

    assertThat(elements).isNotEmpty();
    assertThat(elements.stream().map(e -> e.getTestId()).filter(id -> id != null).toList())
            .contains("ai-accountant-chat-send-btn", "ai-accountant-chat-input");
  }

  @Test(groups = "unit")
  public void locatorFailureAnalyzerShouldExtractOldLocator() {
    var analyzer = new LocatorFailureAnalyzer();
    String trace = "Timeout exceeded waiting for locator(\".old-send-btn\")";
    var parsed = analyzer.extractLocator(trace);

    assertThat(parsed.value()).isEqualTo(".old-send-btn");
    assertThat(analyzer.isLocatorFailure(trace, trace)).isTrue();
  }

  @Test(groups = "unit")
  public void suggestedLocatorGeneratorShouldProposeTestId() throws Exception {
    SelfHealingAgentConfig config = testConfig();
    String html = readFixtureText("agents/selfhealing/dom/AIAccountantSmokeTest_shouldSendMessage.html");
    var elements = new DomSnapshotCollector().parseHtml(html);

    LocatorFailureContext context = LocatorFailureContext.builder()
            .testKey("com.flowiq.ui.smoke.AIAccountantSmokeTest#shouldSendMessage")
            .testName("shouldSendMessage")
            .failureMessage("Timeout waiting for locator(\".old-send-btn\")")
            .stackTrace("waiting for locator(\".old-send-btn\")")
            .oldLocator(".old-send-btn")
            .oldLocatorType(LocatorType.CSS)
            .domElements(elements)
            .build();

    var generator = new SuggestedLocatorGenerator(config, new LocatorFailureAnalyzer(),
            new NoOpSelfHealingLlmProvider());
    LocatorSuggestion suggestion = generator.generate(context).orElseThrow();

    assertThat(suggestion.getSuggestedLocator()).contains("getByTestId");
    assertThat(suggestion.getSuggestedLocator()).contains("ai-accountant-chat-send-btn");
    assertThat(suggestion.getSimilarityScore()).isGreaterThanOrEqualTo(0.45);
    assertThat(suggestion.getRisk()).isEqualTo(LocatorRisk.LOW);
    assertThat(suggestion.getReasoning()).contains("Levenshtein");
  }

  @Test(groups = "unit")
  public void reportShouldContainRequiredFields() throws Exception {
    SelfHealingAgentConfig config = testConfig();
    var result = SelfHealingResult.builder()
            .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
            .failuresAnalyzed(1)
            .suggestionsGenerated(1)
            .suggestion(LocatorSuggestion.builder()
                    .testKey("com.flowiq.ui.smoke.AIAccountantSmokeTest#shouldSendMessage")
                    .testName("shouldSendMessage")
                    .oldLocator(".old-send-btn")
                    .suggestedLocator("page.getByTestId('ai-accountant-chat-send-btn')")
                    .suggestedLocatorType(LocatorType.TEST_ID)
                    .confidence(LocatorConfidence.HIGH)
                    .similarityScore(0.91)
                    .reasoning("Matched button via test-id")
                    .risk(LocatorRisk.LOW)
                    .screenshotPath("—")
                    .domSnapshotPath("fixture.html")
                    .build())
            .summaryLine("Analyzed 1 failure")
            .dataSourcesSummary("fixtures")
            .build();

    Path report = new SelfHealingReportGenerator(config).generate(result);
    String content = Files.readString(report);

    assertThat(content).contains("# Self-Healing Locator Report");
    assertThat(content).contains("**Test**");
    assertThat(content).contains("**Old Locator**");
    assertThat(content).contains("**Suggested Locator**");
    assertThat(content).contains("**Confidence**");
    assertThat(content).contains("**Reasoning**");
    assertThat(content).contains("**Risk**");
  }

  @Test(groups = "unit")
  public void agentShouldHealLocatorFromFixtures() {
    SelfHealingResult result = new SelfHealingLocatorAgent(testConfig()).run();

    assertThat(result.getFailuresAnalyzed()).isEqualTo(1);
    assertThat(result.getSuggestions()).hasSize(1);
    assertThat(result.getSuggestions().get(0).getSuggestedLocator())
            .contains("ai-accountant-chat-send-btn");
    assertThat(result.getExecutiveSummary()).isNotEmpty();
  }

  private static String readFixtureText(String resource) throws IOException {
    try (InputStream input = SelfHealingLocatorAgentTest.class.getClassLoader().getResourceAsStream(resource)) {
      assertThat(input).as("fixture %s", resource).isNotNull();
      return new String(input.readAllBytes());
    }
  }
}
