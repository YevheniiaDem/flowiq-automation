package com.flowiq.agents.prreview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.analyzer.ChangeAnalyzerPipeline;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiSnapshotStore;
import com.flowiq.agents.prreview.analyzers.PrReviewAnalyzerPipeline;
import com.flowiq.agents.prreview.analyzers.PrReviewVerdictEvaluator;
import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.model.PrReviewVerdict;
import com.flowiq.agents.prreview.model.PullRequestReviewResult;
import com.flowiq.agents.prreview.report.PullRequestReviewReportGenerator;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.PullRequestChangeScanner;
import com.flowiq.agents.prreview.scanner.SourceInventory;
import com.flowiq.agents.prreview.scanner.SourceInventoryScanner;
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
 * Production-grade agent that performs automated Pull Request review and produces
 * QA/Architecture conclusions before test execution.
 */
@Slf4j
public class PullRequestReviewAgent {

    private final PullRequestReviewAgentConfig config;
    private final PullRequestChangeScanner changeScanner;
    private final SourceInventoryScanner inventoryScanner;
    private final TestSourceScanner testSourceScanner;
    private final PrReviewAnalyzerPipeline analyzerPipeline;
    private final PrReviewVerdictEvaluator verdictEvaluator;
    private final PullRequestReviewReportGenerator reportGenerator;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final OpenApiSnapshotStore snapshotStore;
    private final ChangeAnalyzerPipeline changeAnalyzerPipeline;

    public PullRequestReviewAgent() {
        this(ConfigFactory.create(PullRequestReviewAgentConfig.class));
    }

    public PullRequestReviewAgent(PullRequestReviewAgentConfig config) {
        this.config = config;
        this.changeScanner = new PullRequestChangeScanner(config);
        this.inventoryScanner = new SourceInventoryScanner(config);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        this.testSourceScanner = new TestSourceScanner(gapConfig);
        this.analyzerPipeline = new PrReviewAnalyzerPipeline();
        this.verdictEvaluator = new PrReviewVerdictEvaluator();
        this.reportGenerator = new PullRequestReviewReportGenerator(config);
        this.objectMapper = new ObjectMapper();
        AgentConfig agentConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(agentConfig, objectMapper);
        this.snapshotStore = new OpenApiSnapshotStore(agentConfig, objectMapper);
        this.changeAnalyzerPipeline = ChangeAnalyzerPipeline.defaultPipeline();
    }

    public PullRequestReviewResult run() {
        log.info("Starting PullRequestReviewAgent");
        List<String> changedFiles = changeScanner.loadChangedFiles();
        List<ApiChange> apiChanges = detectApiChanges();
        return run(changedFiles, apiChanges, summarizeSources(changedFiles, apiChanges));
    }

    public PullRequestReviewResult run(List<String> changedFiles,
                                       List<ApiChange> apiChanges,
                                       String dataSourcesSummary) {
        List<PrChangedArtifact> artifacts = changeScanner.scan(changedFiles, apiChanges);
        List<ScannedTestReference> tests = testSourceScanner.scan();
        SourceInventory inventory = inventoryScanner.scan();

        var contextBuilder = PrReviewContext.builder()
                .changedFiles(changedFiles)
                .apiChanges(apiChanges)
                .testReferences(tests)
                .sourceInventory(inventory)
                .dataSourcesSummary(dataSourcesSummary);
        artifacts.forEach(contextBuilder::artifact);
        PrReviewContext context = contextBuilder.build();

        List<PrReviewFinding> findings = analyzerPipeline.analyze(context);
        PrReviewVerdict verdict = verdictEvaluator.evaluate(findings);
        String recommendation = verdictEvaluator.buildRecommendation(verdict, findings);

        int critical = (int) findings.stream()
                .filter(f -> f.getSeverity() == PrReviewSeverity.CRITICAL).count();
        int high = (int) findings.stream()
                .filter(f -> f.getSeverity() == PrReviewSeverity.HIGH).count();

        var resultBuilder = PullRequestReviewResult.builder()
                .reviewedAt(Instant.now())
                .verdict(verdict)
                .findingsCount(findings.size())
                .criticalFindings(critical)
                .highFindings(high)
                .recommendation(recommendation)
                .dataSourcesSummary(dataSourcesSummary)
                .pullRequestSummary(buildPrSummary(changedFiles, apiChanges, artifacts.size()));
        changedFiles.forEach(resultBuilder::changedFile);
        findings.forEach(resultBuilder::finding);
        buildExecutiveSummary(verdict, findings, changedFiles).forEach(resultBuilder::summaryLine);

        PullRequestReviewResult result = resultBuilder.build();
        Path reportPath = reportGenerator.generate(result);
        log.info("PR review complete. Verdict={}, findings={}, report={}",
                verdict, findings.size(), reportPath.toAbsolutePath());
        return result;
    }

    private List<ApiChange> detectApiChanges() {
        try {
            JsonNode current = loadCurrentOpenApi();
            JsonNode previous = snapshotStore.loadPreviousSnapshot().orElse(current);
            if (current.equals(previous)) {
                return List.of();
            }
            return changeAnalyzerPipeline.analyze(previous, current);
        } catch (Exception e) {
            log.warn("OpenAPI change detection skipped: {}", e.getMessage());
            return List.of();
        }
    }

    private JsonNode loadCurrentOpenApi() throws IOException {
        String snapshot = config.openApiSnapshot();
        if (snapshot != null && !snapshot.isBlank()) {
            Path path = Paths.get(snapshot);
            if (!path.isAbsolute()) {
                path = Paths.get(System.getProperty("user.dir")).resolve(path);
            }
            return objectMapper.readTree(Files.readString(path));
        }
        return openApiFetcher.fetchCurrentSpec();
    }

    private static List<String> buildExecutiveSummary(PrReviewVerdict verdict,
                                                      List<PrReviewFinding> findings,
                                                      List<String> changedFiles) {
        List<String> summary = new ArrayList<>();
        summary.add("Pull Request review verdict: " + verdict + ".");
        summary.add(changedFiles.size() + " changed file(s) analyzed across API, backend, automation, UI, and quality dimensions.");
        summary.add(findings.size() + " finding(s) detected before test execution.");
        findings.stream()
                .filter(f -> f.getSeverity() == PrReviewSeverity.CRITICAL)
                .limit(2)
                .forEach(f -> summary.add("CRITICAL: " + f.getTitle() + " at " + f.getLocation()));
        return summary;
    }

    private static String buildPrSummary(List<String> changedFiles,
                                         List<ApiChange> apiChanges,
                                         int artifactCount) {
        return changedFiles.size() + " changed file(s), " + apiChanges.size()
                + " API change(s), " + artifactCount + " review artifact(s).";
    }

    private static String summarizeSources(List<String> changedFiles, List<ApiChange> apiChanges) {
        return "Changed files: " + changedFiles.size()
                + "; API changes: " + apiChanges.size()
                + "; Main source: src/main/java; Tests: src/test/java; Schemas: src/test/resources/schemas";
    }

    public static void main(String[] args) {
        new PullRequestReviewAgent().run();
    }
}
