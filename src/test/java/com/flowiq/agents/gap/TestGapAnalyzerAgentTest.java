package com.flowiq.agents.gap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.gap.analyzer.GapAnalyzerPipeline;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.matrix.CoverageMatrixBuilder;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.GapType;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.report.TestGapReportGenerator;
import com.flowiq.agents.gap.scanner.EndpointMatcher;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.gap.analyzer.GapAnalysisContext;
import com.flowiq.agents.openapi.OpenApiNavigator;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGapAnalyzerAgentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(groups = "unit")
    public void endpointMatcherShouldMatchParameterizedPaths() {
        assertThat(EndpointMatcher.matches("/tasks/{id}", "/tasks/42")).isTrue();
        assertThat(EndpointMatcher.matches("/tasks", "/tasks")).isTrue();
        assertThat(EndpointMatcher.matches("/auth/login", "/tasks")).isFalse();
    }

    @Test(groups = "unit")
    public void testSourceScannerShouldFindRegressionTests() {
        TestGapAgentConfig config = ConfigFactory.create(TestGapAgentConfig.class);
        TestSourceScanner scanner = new TestSourceScanner(config);
        List<ScannedTestReference> references = scanner.scan();

        assertThat(references).isNotEmpty();
        assertThat(references.stream().map(ScannedTestReference::getClassName))
                .anyMatch(name -> name.contains("TasksRegressionTest"));
    }

    @Test(groups = "unit")
    public void coverageMatrixShouldMapTasksEndpoints() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        TestGapAgentConfig config = ConfigFactory.create(TestGapAgentConfig.class);
        List<ScannedTestReference> references = new TestSourceScanner(config).scan();

        List<EndpointCoverage> coverages = new CoverageMatrixBuilder().build(spec, references);
        assertThat(coverages).anyMatch(c -> "/tasks".equals(c.getPath()) && "POST".equals(c.getMethod()));
        assertThat(coverages.stream().filter(c -> c.getPath().startsWith("/tasks")))
                .anyMatch(EndpointCoverage::isRegressionCovered);
    }

    @Test(groups = "unit")
    public void gapPipelineShouldDetectImportsAndDashboardGaps() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        TestGapAgentConfig config = ConfigFactory.create(TestGapAgentConfig.class);
        BusinessImpactPrioritizer prioritizer = new BusinessImpactPrioritizer(config);
        List<ScannedTestReference> references = new TestSourceScanner(config).scan();
        List<EndpointCoverage> coverages = new CoverageMatrixBuilder().build(spec, references);

        GapAnalysisContext context = GapAnalysisContext.builder()
                .openApiSpec(spec)
                .operations(OpenApiNavigator.getOperations(spec))
                .testReferences(references)
                .endpointCoverages(coverages)
                .moduleBusinessImpact(prioritizer.moduleImpacts())
                .uiExpectedModules(prioritizer.uiExpectedModules())
                .build();

        List<TestGap> gaps = new GapAnalyzerPipeline(prioritizer).analyze(context);

        assertThat(gaps).anyMatch(g -> "imports".equals(g.getModule()));
        assertThat(gaps).anyMatch(g -> g.getType() == GapType.NO_TEST_COVERAGE
                || g.getType() == GapType.MISSING_REGRESSION_COVERAGE);
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        TestGapAgentConfig config = ConfigFactory.create(TestGapAgentConfig.class);
        var result = TestGapAnalysisResult.builder()
                .analyzedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .overallCoveragePercent(72.5)
                .gap(TestGap.builder()
                        .type(GapType.MISSING_CONTRACT_COVERAGE)
                        .severity(GapSeverity.HIGH)
                        .module("tasks")
                        .path("/tasks")
                        .method("GET")
                        .description("Missing contract test")
                        .recommendedTest("TasksContractTest — cover GET /tasks")
                        .build())
                .recommendedTest("TasksContractTest — cover GET /tasks")
                .build();

        Path reportPath = new TestGapReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Test Gap Analysis");
        assertThat(content).contains("## Coverage %");
        assertThat(content).contains("## Module Risk");
        assertThat(content).contains("## Missing Tests");
        assertThat(content).contains("## Recommended New Tests");
        assertThat(content).contains("**Overall coverage: 72.5%**");
    }

    @Test(groups = "unit")
    public void agentShouldRunWithOpenApiFixture() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        TestGapAnalysisResult result = new TestGapAnalyzerAgent().run(spec);

        assertThat(result.getOverallCoveragePercent()).isGreaterThan(0.0);
        assertThat(result.getGaps()).isNotEmpty();
        assertThat(result.getRecommendedTests()).isNotEmpty();
        assertThat(result.getGaps().stream().map(TestGap::getSeverity))
                .containsAnyOf(GapSeverity.CRITICAL, GapSeverity.HIGH, GapSeverity.MEDIUM, GapSeverity.LOW);
    }

    private static JsonNode readFixture(String resource) throws Exception {
        try (InputStream input = TestGapAnalyzerAgentTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).as("fixture %s", resource).isNotNull();
            return MAPPER.readTree(input);
        }
    }
}
