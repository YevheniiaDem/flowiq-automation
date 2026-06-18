package com.flowiq.agents.gap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.gap.analyzer.GapAnalysisContext;
import com.flowiq.agents.gap.analyzer.GapAnalyzerPipeline;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.matrix.CoverageMatrixBuilder;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.ModuleCoverage;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.report.TestGapReportGenerator;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiNavigator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI agent that scans the OpenAPI specification and test sources to identify
 * insufficient automated test coverage across contract, smoke, regression, and UI suites.
 */
@Slf4j
public class TestGapAnalyzerAgent {

    private final TestGapAgentConfig config;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final TestSourceScanner testSourceScanner;
    private final CoverageMatrixBuilder coverageMatrixBuilder;
    private final BusinessImpactPrioritizer prioritizer;
    private final GapAnalyzerPipeline gapAnalyzerPipeline;
    private final TestGapReportGenerator reportGenerator;

    public TestGapAnalyzerAgent() {
        this(ConfigFactory.create(TestGapAgentConfig.class));
    }

    public TestGapAnalyzerAgent(TestGapAgentConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        AgentConfig openApiConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(openApiConfig, objectMapper);
        this.testSourceScanner = new TestSourceScanner(config);
        this.coverageMatrixBuilder = new CoverageMatrixBuilder();
        this.prioritizer = new BusinessImpactPrioritizer(config);
        this.gapAnalyzerPipeline = new GapAnalyzerPipeline(prioritizer);
        this.reportGenerator = new TestGapReportGenerator(config);
    }

    public TestGapAnalysisResult run() {
        return run(loadOpenApiSpec());
    }

    public TestGapAnalysisResult run(JsonNode openApiSpec) {
        log.info("Starting TestGapAnalyzerAgent");

        List<ScannedTestReference> testReferences = testSourceScanner.scan();
        List<EndpointCoverage> endpointCoverages = coverageMatrixBuilder.build(openApiSpec, testReferences);

        GapAnalysisContext context = GapAnalysisContext.builder()
                .openApiSpec(openApiSpec)
                .operations(OpenApiNavigator.getOperations(openApiSpec))
                .testReferences(testReferences)
                .endpointCoverages(endpointCoverages)
                .moduleBusinessImpact(prioritizer.moduleImpacts())
                .uiExpectedModules(prioritizer.uiExpectedModules())
                .build();

        List<TestGap> gaps = gapAnalyzerPipeline.analyze(context);
        List<ModuleCoverage> modules = buildModuleCoverage(endpointCoverages, gaps);
        double overallCoverage = endpointCoverages.isEmpty() ? 100.0
                : endpointCoverages.stream()
                .mapToDouble(e -> e.coverageScore(prioritizer.uiExpected(e.getModule())))
                .average()
                .orElse(0.0);

        List<String> recommendedTests = gaps.stream()
                .sorted(Comparator.comparing(TestGap::getSeverity))
                .map(TestGap::getRecommendedTest)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        TestGapAnalysisResult result = TestGapAnalysisResult.builder()
                .analyzedAt(Instant.now())
                .overallCoveragePercent(overallCoverage)
                .modules(modules)
                .gaps(gaps)
                .recommendedTests(recommendedTests)
                .build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Test gap analysis complete. {} endpoint(s), {} gap(s), coverage={}%, report={}",
                endpointCoverages.size(), gaps.size(), String.format("%.1f", overallCoverage),
                reportPath.toAbsolutePath());

        return result;
    }

    private List<ModuleCoverage> buildModuleCoverage(List<EndpointCoverage> endpoints, List<TestGap> gaps) {
        Map<String, List<EndpointCoverage>> byModule = endpoints.stream()
                .collect(Collectors.groupingBy(EndpointCoverage::getModule, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<TestGap>> gapsByModule = gaps.stream()
                .collect(Collectors.groupingBy(TestGap::getModule));

        List<ModuleCoverage> modules = new ArrayList<>();
        for (Map.Entry<String, List<EndpointCoverage>> entry : byModule.entrySet()) {
            String module = entry.getKey();
            List<EndpointCoverage> moduleEndpoints = entry.getValue();
            boolean uiExpected = prioritizer.uiExpected(module);
            double coveragePercent = moduleEndpoints.stream()
                    .mapToDouble(e -> e.coverageScore(uiExpected))
                    .average()
                    .orElse(0.0);
            int covered = (int) moduleEndpoints.stream().filter(EndpointCoverage::hasAnyCoverage).count();

            modules.add(ModuleCoverage.builder()
                    .module(module)
                    .businessImpact(prioritizer.businessImpactFor(module))
                    .totalEndpoints(moduleEndpoints.size())
                    .coveredEndpoints(covered)
                    .coveragePercent(coveragePercent)
                    .endpoints(moduleEndpoints)
                    .gaps(gapsByModule.getOrDefault(module, List.of()))
                    .build());
        }
        modules.sort(Comparator.comparing(ModuleCoverage::getBusinessImpact)
                .thenComparing(Comparator.comparing(ModuleCoverage::getCoveragePercent)));
        return modules;
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
        new TestGapAnalyzerAgent().run();
    }
}
