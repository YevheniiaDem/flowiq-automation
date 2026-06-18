package com.flowiq.agents.traceability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.matrix.CoverageMatrixBuilder;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.traceability.analyzer.TraceabilityIssueAnalyzer;
import com.flowiq.agents.traceability.config.TraceabilityAgentConfig;
import com.flowiq.agents.traceability.docs.DocumentationFeatureIndex;
import com.flowiq.agents.traceability.matrix.FeatureTraceabilityMatrixBuilder;
import com.flowiq.agents.traceability.model.BusinessFeature;
import com.flowiq.agents.traceability.model.FeatureTraceabilityRow;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;
import com.flowiq.agents.traceability.model.TraceabilityIssue;
import com.flowiq.agents.traceability.model.TraceabilityIssueType;
import com.flowiq.agents.traceability.report.TraceabilityMatrixReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * AI agent that builds full traceability between business features (docs/),
 * OpenAPI endpoints, and automated test suites (smoke, regression, contract, UI).
 */
@Slf4j
public class RequirementsTraceabilityAgent {

    private final TraceabilityAgentConfig config;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final DocumentationFeatureIndex documentationIndex;
    private final TestSourceScanner testSourceScanner;
    private final CoverageMatrixBuilder coverageMatrixBuilder;
    private final BusinessImpactPrioritizer prioritizer;
    private final FeatureTraceabilityMatrixBuilder matrixBuilder;
    private final TraceabilityIssueAnalyzer issueAnalyzer;
    private final TraceabilityMatrixReportGenerator reportGenerator;

    public RequirementsTraceabilityAgent() {
        this(ConfigFactory.create(TraceabilityAgentConfig.class));
    }

    public RequirementsTraceabilityAgent(TraceabilityAgentConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        AgentConfig openApiConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(openApiConfig, objectMapper);
        this.documentationIndex = new DocumentationFeatureIndex(config);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        this.testSourceScanner = new TestSourceScanner(gapConfig);
        this.coverageMatrixBuilder = new CoverageMatrixBuilder();
        this.prioritizer = new BusinessImpactPrioritizer(gapConfig);
        this.matrixBuilder = new FeatureTraceabilityMatrixBuilder(prioritizer);
        this.issueAnalyzer = new TraceabilityIssueAnalyzer(config);
        this.reportGenerator = new TraceabilityMatrixReportGenerator(config);
    }

    public TraceabilityAnalysisResult run() {
        log.info("Starting RequirementsTraceabilityAgent");
        return run(loadOpenApiSpec());
    }

    public TraceabilityAnalysisResult run(JsonNode openApiSpec) {
        List<BusinessFeature> documentedFeatures = documentationIndex.index();
        List<ScannedTestReference> testReferences = testSourceScanner.scan();
        List<EndpointCoverage> endpointCoverages = coverageMatrixBuilder.build(openApiSpec, testReferences);
        List<FeatureTraceabilityRow> matrix = matrixBuilder.build(documentedFeatures, endpointCoverages, testReferences);
        List<TraceabilityIssue> issues = issueAnalyzer.analyze(matrix, documentedFeatures, testReferences);

        double overallCoverage = matrix.isEmpty() ? 100.0
                : matrix.stream().mapToDouble(FeatureTraceabilityRow::getCoveragePercent).average().orElse(0.0);

        String dataSources = String.format(
                "docs/ (%d features); OpenAPI (%d endpoints); test sources (%d references)",
                documentedFeatures.size(), endpointCoverages.size(), testReferences.size());

        var resultBuilder = TraceabilityAnalysisResult.builder()
                .analyzedAt(Instant.now())
                .overallCoveragePercent(Math.round(overallCoverage * 10.0) / 10.0)
                .featureCount(matrix.size())
                .documentedFeatureCount((int) matrix.stream().filter(FeatureTraceabilityRow::isDocumentedInDocs).count())
                .openApiEndpointCount(endpointCoverages.size())
                .matrix(matrix)
                .issues(issues)
                .dataSourcesSummary(dataSources);
        buildExecutiveSummary(matrix, issues, overallCoverage).forEach(resultBuilder::summaryLine);
        TraceabilityAnalysisResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Traceability analysis complete. {} feature(s), {}% coverage, report={}",
                matrix.size(), result.getOverallCoveragePercent(), reportPath.toAbsolutePath());
        return result;
    }

    private List<String> buildExecutiveSummary(List<FeatureTraceabilityRow> matrix,
                                               List<TraceabilityIssue> issues,
                                               double overallCoverage) {
        List<String> summary = new ArrayList<>();
        summary.add(String.format("Overall feature traceability coverage is %.1f%% across %d business features.",
                overallCoverage, matrix.size()));

        long missing = issues.stream().filter(i -> i.getType() == TraceabilityIssueType.MISSING_COVERAGE).count();
        long broken = issues.stream().filter(i -> i.getType() == TraceabilityIssueType.BROKEN_TRACEABILITY).count();
        long highRisk = issues.stream().filter(i -> i.getType() == TraceabilityIssueType.HIGH_RISK).count();
        summary.add(String.format("%d missing coverage, %d broken traceability, %d high-risk feature(s) identified.",
                missing, broken, highRisk));

        matrix.stream()
                .filter(FeatureTraceabilityRow::isHighRisk)
                .limit(3)
                .forEach(row -> summary.add(String.format("High-risk: %s (%.0f%%, %s impact).",
                        row.getFeatureName(), row.getCoveragePercent(), row.getBusinessImpact())));

        long undocumented = matrix.stream().filter(r -> !r.isDocumentedInDocs()).count();
        if (undocumented > 0) {
            summary.add(undocumented + " OpenAPI module(s) lack documentation trace in docs/.");
        }

        long fullyTraced = matrix.stream()
                .filter(r -> r.getMissingSuites().isEmpty() && r.isDocumentedInDocs())
                .count();
        summary.add(fullyTraced + " feature(s) have full docs → API → test traceability.");

        return summary;
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
        new RequirementsTraceabilityAgent().run();
    }
}
