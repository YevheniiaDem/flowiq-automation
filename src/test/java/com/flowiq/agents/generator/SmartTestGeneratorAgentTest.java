package com.flowiq.agents.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.generator.analysis.EndpointCoverageAnalyzer;
import com.flowiq.agents.generator.config.SmartTestGeneratorConfig;
import com.flowiq.agents.generator.generator.ScenarioGeneratorPipeline;
import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;
import com.flowiq.agents.generator.report.GeneratedScenarioReportGenerator;
import com.flowiq.agents.generator.schema.JsonSchemaIndex;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SmartTestGeneratorAgentTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(groups = "unit")
    public void jsonSchemaIndexShouldLoadProjectSchemas() {
        SmartTestGeneratorConfig config = ConfigFactory.create(SmartTestGeneratorConfig.class);
        JsonSchemaIndex index = new JsonSchemaIndex(config, MAPPER);

        assertThat(index.all()).isNotEmpty();
        assertThat(index.findForEndpoint("/tasks", "GET")).isPresent();
    }

    @Test(groups = "unit")
    public void coverageAnalyzerShouldDetectUncoveredScenarioTypes() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        SmartTestGeneratorConfig config = ConfigFactory.create(SmartTestGeneratorConfig.class);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        List<ScannedTestReference> tests = new TestSourceScanner(gapConfig).scan();
        JsonSchemaIndex schemaIndex = new JsonSchemaIndex(config, MAPPER);

        List<EndpointTestContext> contexts = new EndpointCoverageAnalyzer()
                .analyze(spec, tests, schemaIndex);

        assertThat(contexts).isNotEmpty();
        assertThat(contexts).anyMatch(ctx -> ctx.getCoveredScenarioTypes().isEmpty()
                || !ctx.getCoveredScenarioTypes().containsAll(EnumSet.allOf(ScenarioType.class)));
    }

    @Test(groups = "unit")
    public void scenarioPipelineShouldGenerateMultipleScenarioTypes() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        SmartTestGeneratorConfig config = ConfigFactory.create(SmartTestGeneratorConfig.class);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        List<ScannedTestReference> tests = new TestSourceScanner(gapConfig).scan();
        JsonSchemaIndex schemaIndex = new JsonSchemaIndex(config, MAPPER);
        List<EndpointTestContext> contexts = new EndpointCoverageAnalyzer()
                .analyze(spec, tests, schemaIndex);

        List<TestScenario> scenarios = new ScenarioGeneratorPipeline(config).generate(contexts);

        assertThat(scenarios).isNotEmpty();
        assertThat(scenarios.stream().map(TestScenario::getType).distinct().toList())
                .containsAnyOf(ScenarioType.POSITIVE, ScenarioType.NEGATIVE,
                        ScenarioType.BOUNDARY, ScenarioType.AUTHORIZATION, ScenarioType.SECURITY);
    }

    @Test(groups = "unit")
    public void generatedScenarioShouldContainRequiredFields() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        SmartTestGeneratorConfig config = ConfigFactory.create(SmartTestGeneratorConfig.class);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        List<ScannedTestReference> tests = new TestSourceScanner(gapConfig).scan();
        JsonSchemaIndex schemaIndex = new JsonSchemaIndex(config, MAPPER);
        List<EndpointTestContext> contexts = new EndpointCoverageAnalyzer()
                .analyze(spec, tests, schemaIndex);
        List<TestScenario> scenarios = new ScenarioGeneratorPipeline(config).generate(contexts);

        TestScenario sample = scenarios.get(0);
        assertThat(sample.getTitle()).isNotBlank();
        assertThat(sample.getPreconditions()).isNotEmpty();
        assertThat(sample.getSteps()).isNotEmpty();
        assertThat(sample.getExpectedResult()).isNotBlank();
        assertThat(sample.getPriority()).isNotNull();
        assertThat(sample.getRisk()).isNotNull();
    }

    @Test(groups = "unit")
    public void reportGeneratorShouldContainRequiredSections() throws Exception {
        SmartTestGeneratorConfig config = ConfigFactory.create(SmartTestGeneratorConfig.class);
        var result = ScenarioGenerationResult.builder()
                .generatedAt(Instant.parse("2026-06-17T12:00:00Z"))
                .endpointsAnalyzed(5)
                .schemasLoaded(10)
                .existingTestReferences(42)
                .scenario(TestScenario.builder()
                        .id("tasks-get-positive-happy-path")
                        .title("Happy path — GET /tasks")
                        .type(ScenarioType.POSITIVE)
                        .module("tasks")
                        .endpoint("/tasks")
                        .httpMethod("GET")
                        .precondition("User is authenticated")
                        .step("Send GET /tasks")
                        .expectedResult("HTTP 200 OK")
                        .priority(com.flowiq.agents.generator.model.ScenarioPriority.P1)
                        .risk(com.flowiq.agents.generator.model.ScenarioRisk.HIGH)
                        .build())
                .scenariosByType(java.util.Map.of(ScenarioType.POSITIVE, 1L))
                .dataSourcesSummary("OpenAPI (5 operations); JSON Schemas (10 files); Test sources (42 references)")
                .build();

        Path reportPath = new GeneratedScenarioReportGenerator(config).generate(result);
        String content = Files.readString(reportPath);

        assertThat(content).contains("# Generated Test Scenarios");
        assertThat(content).contains("## Summary");
        assertThat(content).contains("**Preconditions**");
        assertThat(content).contains("**Steps**");
        assertThat(content).contains("**Expected Result**");
        assertThat(content).contains("| **Priority** |");
        assertThat(content).contains("| **Risk** |");
    }

    @Test(groups = "unit")
    public void agentShouldRunWithOpenApiFixture() throws Exception {
        JsonNode spec = readFixture("agents/gap-openapi.json");
        ScenarioGenerationResult result = new SmartTestGeneratorAgent().run(spec);

        assertThat(result.getEndpointsAnalyzed()).isGreaterThan(0);
        assertThat(result.getSchemasLoaded()).isGreaterThan(0);
        assertThat(result.getScenarios()).isNotEmpty();
        assertThat(result.getScenariosByType().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(result.getScenarios().size());
    }

    private static JsonNode readFixture(String resource) throws Exception {
        try (InputStream input = SmartTestGeneratorAgentTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).as("fixture %s", resource).isNotNull();
            return MAPPER.readTree(input);
        }
    }
}
