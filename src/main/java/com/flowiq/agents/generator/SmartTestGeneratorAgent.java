package com.flowiq.agents.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.generator.analysis.EndpointCoverageAnalyzer;
import com.flowiq.agents.generator.config.SmartTestGeneratorConfig;
import com.flowiq.agents.generator.generator.ScenarioGeneratorPipeline;
import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioGenerationResult;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.model.TestScenario;
import com.flowiq.agents.generator.report.GeneratedScenarioReportGenerator;
import com.flowiq.agents.generator.schema.JsonSchemaIndex;
import com.flowiq.agents.openapi.OpenApiFetcher;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI agent that analyzes OpenAPI contracts, JSON Schemas, and existing tests
 * to produce implementation-ready QA scenarios for uncovered test cases.
 */
@Slf4j
public class SmartTestGeneratorAgent {

    private final SmartTestGeneratorConfig config;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final TestSourceScanner testSourceScanner;
    private final EndpointCoverageAnalyzer coverageAnalyzer;
    private final ScenarioGeneratorPipeline scenarioPipeline;
    private final GeneratedScenarioReportGenerator reportGenerator;

    public SmartTestGeneratorAgent() {
        this(ConfigFactory.create(SmartTestGeneratorConfig.class));
    }

    public SmartTestGeneratorAgent(SmartTestGeneratorConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        AgentConfig openApiConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(openApiConfig, objectMapper);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        this.testSourceScanner = new TestSourceScanner(gapConfig);
        this.coverageAnalyzer = new EndpointCoverageAnalyzer();
        this.scenarioPipeline = new ScenarioGeneratorPipeline(config);
        this.reportGenerator = new GeneratedScenarioReportGenerator(config);
    }

    public ScenarioGenerationResult run() {
        log.info("Starting SmartTestGeneratorAgent");
        JsonNode openApiSpec = loadOpenApiSpec();
        return run(openApiSpec);
    }

    public ScenarioGenerationResult run(JsonNode openApiSpec) {
        JsonSchemaIndex schemaIndex = new JsonSchemaIndex(config, objectMapper);
        List<ScannedTestReference> tests = testSourceScanner.scan();
        List<EndpointTestContext> contexts = coverageAnalyzer.analyze(openApiSpec, tests, schemaIndex);
        List<TestScenario> scenarios = scenarioPipeline.generate(contexts);

        Map<ScenarioType, Long> byType = scenarios.stream()
                .collect(Collectors.groupingBy(TestScenario::getType, Collectors.counting()));

        String dataSources = String.format(
                "OpenAPI (%d operations); JSON Schemas (%d files); Test sources (%d references)",
                contexts.size(), schemaIndex.all().size(), tests.size());

        ScenarioGenerationResult result = ScenarioGenerationResult.builder()
                .generatedAt(Instant.now())
                .endpointsAnalyzed(contexts.size())
                .schemasLoaded(schemaIndex.all().size())
                .existingTestReferences(tests.size())
                .scenarios(scenarios)
                .scenariosByType(byType)
                .dataSourcesSummary(dataSources)
                .build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Generation complete. {} scenario(s), report={}",
                scenarios.size(), reportPath.toAbsolutePath());
        return result;
    }

    private JsonNode loadOpenApiSpec() {
        String snapshot = config.openApiSnapshot();
        if (snapshot != null && !snapshot.isBlank()) {
            return loadSnapshot(snapshot);
        }
        return openApiFetcher.fetchCurrentSpec();
    }

    private JsonNode loadSnapshot(String location) {
        Path path = Paths.get(location);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        try {
            log.info("Loading OpenAPI spec from snapshot {}", path);
            return objectMapper.readTree(Files.readString(path));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load OpenAPI snapshot from " + path, e);
        }
    }

    public static void main(String[] args) {
        new SmartTestGeneratorAgent().run();
    }
}
