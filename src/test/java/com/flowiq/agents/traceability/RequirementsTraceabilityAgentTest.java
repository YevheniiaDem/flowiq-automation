package com.flowiq.agents.traceability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.matrix.CoverageMatrixBuilder;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.traceability.config.TraceabilityAgentConfig;
import com.flowiq.agents.traceability.docs.DocumentationFeatureIndex;
import com.flowiq.agents.traceability.docs.ModuleNameNormalizer;
import com.flowiq.agents.traceability.docs.RegressionDocExtractor;
import com.flowiq.agents.traceability.matrix.FeatureTraceabilityMatrixBuilder;
import com.flowiq.agents.traceability.model.BusinessFeature;
import com.flowiq.agents.traceability.model.FeatureTraceabilityRow;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;
import com.flowiq.agents.traceability.model.TraceabilityIssueType;
import com.flowiq.agents.traceability.report.TraceabilityMatrixReportGenerator;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RequirementsTraceabilityAgentTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test(groups = "unit")
  public void moduleNameNormalizerShouldMapDisplayNames() {
    assertThat(ModuleNameNormalizer.toSlug("Business Guide")).isEqualTo("business-guide");
    assertThat(ModuleNameNormalizer.toSlug("AI Accountant")).isEqualTo("ai-accountant");
    assertThat(ModuleNameNormalizer.toDisplayName("tasks")).isEqualTo("Tasks");
  }

  @Test(groups = "unit")
  public void regressionDocExtractorShouldParseModules() throws Exception {
    String content = readFixtureText("agents/traceability/sample-regression-doc.md");
    List<BusinessFeature> features = new RegressionDocExtractor().extract(content);

    assertThat(features).extracting(BusinessFeature::getModule)
            .contains("auth", "tasks", "transactions");
  }

  @Test(groups = "unit")
  public void documentationIndexShouldLoadProjectDocs() {
    TraceabilityAgentConfig config = ConfigFactory.create(TraceabilityAgentConfig.class);
    List<BusinessFeature> features = new DocumentationFeatureIndex(config).index();

    assertThat(features).isNotEmpty();
    assertThat(features.stream().map(BusinessFeature::getModule))
            .containsAnyOf("auth", "tasks", "transactions", "reports");
  }

  @Test(groups = "unit")
  public void matrixBuilderShouldMapEndpointsAndSuites() throws Exception {
    JsonNode spec = readFixtureJson("agents/gap-openapi.json");
    TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
    List<ScannedTestReference> tests = new TestSourceScanner(gapConfig).scan();
    List<EndpointCoverage> endpoints = new CoverageMatrixBuilder().build(spec, tests);
    List<BusinessFeature> docs = List.of(
            BusinessFeature.builder().module("tasks").displayName("Tasks").docSource("test").build());

    List<FeatureTraceabilityRow> matrix = new FeatureTraceabilityMatrixBuilder(
            new BusinessImpactPrioritizer(gapConfig))
            .build(docs, endpoints, tests);

    assertThat(matrix).anyMatch(row -> "tasks".equals(row.getModule()));
    assertThat(matrix.stream().filter(r -> "tasks".equals(r.getModule())).findFirst())
            .hasValueSatisfying(row -> {
              assertThat(row.getEndpointsSummary()).contains("/tasks");
              assertThat(row.getCoveragePercent()).isGreaterThan(0.0);
            });
  }

  @Test(groups = "unit")
  public void reportShouldContainMatrixAndExecutiveSummary() throws Exception {
    TraceabilityAgentConfig config = ConfigFactory.create(TraceabilityAgentConfig.class);
    var result = TraceabilityAnalysisResult.builder()
            .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
            .overallCoveragePercent(82.5)
            .featureCount(3)
            .documentedFeatureCount(2)
            .openApiEndpointCount(10)
            .matrixRow(FeatureTraceabilityRow.builder()
                    .module("auth")
                    .featureName("Auth")
                    .endpointsSummary("POST /auth/login")
                    .smokeCovered(true)
                    .regressionCovered(true)
                    .contractCovered(true)
                    .uiCovered(false)
                    .smokeTests("AuthSmokeApiTest")
                    .regressionTests("AuthRegressionTest")
                    .contractTests("AuthContractTest")
                    .uiTests("—")
                    .coveragePercent(75.0)
                    .endpointCount(2)
                    .documentedInDocs(true)
                    .businessImpact(com.flowiq.agents.gap.model.GapSeverity.CRITICAL)
                    .build())
            .summaryLine("Overall feature traceability coverage is 82.5%.")
            .dataSourcesSummary("test")
            .build();

    Path report = new TraceabilityMatrixReportGenerator(config).generate(result);
    String content = Files.readString(report);

    assertThat(content).contains("# Requirements Traceability Matrix");
    assertThat(content).contains("## Executive Summary");
    assertThat(content).contains("## Traceability Matrix");
    assertThat(content).contains("| Feature | Endpoint | Smoke | Regression | Contract | UI |");
    assertThat(content).contains("## Missing Coverage");
    assertThat(content).contains("## Broken Traceability");
    assertThat(content).contains("## High-Risk Features");
  }

  @Test(groups = "unit")
  public void agentShouldRunWithOpenApiFixture() throws Exception {
    JsonNode spec = readFixtureJson("agents/gap-openapi.json");
    TraceabilityAnalysisResult result = new RequirementsTraceabilityAgent().run(spec);

    assertThat(result.getFeatureCount()).isGreaterThan(0);
    assertThat(result.getOverallCoveragePercent()).isGreaterThan(0.0);
    assertThat(result.getMatrix()).isNotEmpty();
    assertThat(result.getMatrix()).allMatch(row -> row.getFeatureName() != null);
    assertThat(result.getExecutiveSummary()).isNotEmpty();
  }

  @Test(groups = "unit")
  public void agentShouldIdentifyTraceabilityIssues() throws Exception {
    JsonNode spec = readFixtureJson("agents/gap-openapi.json");
    TraceabilityAnalysisResult result = new RequirementsTraceabilityAgent().run(spec);

    assertThat(result.getIssues().stream().map(i -> i.getType()))
            .containsAnyOf(TraceabilityIssueType.MISSING_COVERAGE,
                    TraceabilityIssueType.BROKEN_TRACEABILITY,
                    TraceabilityIssueType.HIGH_RISK);
  }

  private static JsonNode readFixtureJson(String resource) throws Exception {
    try (InputStream input = RequirementsTraceabilityAgentTest.class.getClassLoader()
            .getResourceAsStream(resource)) {
      assertThat(input).as("fixture %s", resource).isNotNull();
      return MAPPER.readTree(input);
    }
  }

  private static String readFixtureText(String resource) throws IOException {
    try (InputStream input = RequirementsTraceabilityAgentTest.class.getClassLoader()
            .getResourceAsStream(resource)) {
      assertThat(input).as("fixture %s", resource).isNotNull();
      return new String(input.readAllBytes());
    }
  }
}
