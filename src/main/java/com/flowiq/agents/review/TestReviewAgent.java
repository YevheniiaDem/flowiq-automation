package com.flowiq.agents.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.analyzer.ChangeAnalyzerPipeline;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiSnapshotStore;
import com.flowiq.agents.review.analyzer.CoverageImpactAnalyzer;
import com.flowiq.agents.review.analyzer.MissingTestAnalyzer;
import com.flowiq.agents.review.config.TestReviewAgentConfig;
import com.flowiq.agents.review.model.CoverageStatus;
import com.flowiq.agents.review.model.FeatureChange;
import com.flowiq.agents.review.model.FeatureReviewItem;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import com.flowiq.agents.review.report.ReviewCommentGenerator;
import com.flowiq.agents.review.scanner.FeatureChangeScanner;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AI agent that reviews Pull Request changes and assesses test coverage quality
 * for new endpoints, DTOs, controllers, and services.
 */
@Slf4j
public class TestReviewAgent {

    private final TestReviewAgentConfig config;
    private final FeatureChangeScanner featureChangeScanner;
    private final TestSourceScanner testSourceScanner;
    private final CoverageImpactAnalyzer coverageImpactAnalyzer;
    private final MissingTestAnalyzer missingTestAnalyzer;
    private final ReviewCommentGenerator reportGenerator;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final OpenApiSnapshotStore snapshotStore;
    private final ChangeAnalyzerPipeline changeAnalyzerPipeline;

    public TestReviewAgent() {
        this(ConfigFactory.create(TestReviewAgentConfig.class));
    }

    public TestReviewAgent(TestReviewAgentConfig config) {
        this.config = config;
        this.featureChangeScanner = new FeatureChangeScanner(config);
        TestGapAgentConfig gapConfig = ConfigFactory.create(TestGapAgentConfig.class);
        this.testSourceScanner = new TestSourceScanner(gapConfig);
        BusinessImpactPrioritizer prioritizer = new BusinessImpactPrioritizer(gapConfig);
        this.coverageImpactAnalyzer = new CoverageImpactAnalyzer(prioritizer);
        this.missingTestAnalyzer = new MissingTestAnalyzer(config, prioritizer, coverageImpactAnalyzer);
        this.reportGenerator = new ReviewCommentGenerator(config);
        this.objectMapper = new ObjectMapper();
        AgentConfig agentConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(agentConfig, objectMapper);
        this.snapshotStore = new OpenApiSnapshotStore(agentConfig, objectMapper);
        this.changeAnalyzerPipeline = ChangeAnalyzerPipeline.defaultPipeline();
    }

    public TestReviewResult run() {
        log.info("Starting TestReviewAgent");
        List<String> changedFiles = featureChangeScanner.loadChangedFiles();
        List<ApiChange> apiChanges = detectApiChanges();
        return run(changedFiles, apiChanges, summarizeSources(changedFiles, apiChanges));
    }

    public TestReviewResult run(List<String> changedFiles,
                                List<ApiChange> apiChanges,
                                String dataSourcesSummary) {
        List<FeatureChange> features = featureChangeScanner.scan(changedFiles, apiChanges);
        List<ScannedTestReference> tests = testSourceScanner.scan();

        List<FeatureReviewItem> reviews = new ArrayList<>();
        for (FeatureChange feature : features) {
            CoverageStatus coverage = coverageImpactAnalyzer.analyze(feature, tests);
            reviews.add(missingTestAnalyzer.analyze(feature, coverage));
        }
        reviews.sort(Comparator.comparingInt((FeatureReviewItem r) -> verdictOrder(r.getVerdict()))
                .thenComparing(item -> item.getRisk().ordinal()));

        ReviewVerdict overall = overallVerdict(reviews);
        int rejected = (int) reviews.stream().filter(r -> r.getVerdict() == ReviewVerdict.REJECTED).count();
        int withRisk = (int) reviews.stream().filter(r -> r.getVerdict() == ReviewVerdict.APPROVED_WITH_RISK).count();

        var resultBuilder = TestReviewResult.builder()
                .reviewedAt(Instant.now())
                .overallVerdict(overall)
                .featuresReviewed(reviews.size())
                .rejectedCount(rejected)
                .approvedWithRiskCount(withRisk)
                .features(reviews)
                .dataSourcesSummary(dataSourcesSummary)
                .pullRequestSummary(buildPrSummary(changedFiles, apiChanges));
        buildExecutiveSummary(reviews, overall).forEach(resultBuilder::summaryLine);
        TestReviewResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Test review complete. Verdict={}, features={}, report={}",
                overall, reviews.size(), reportPath.toAbsolutePath());
        return result;
    }

    private static int verdictOrder(ReviewVerdict verdict) {
        return switch (verdict) {
            case REJECTED -> 0;
            case APPROVED_WITH_RISK -> 1;
            case APPROVED -> 2;
        };
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

    private static ReviewVerdict overallVerdict(List<FeatureReviewItem> reviews) {
        if (reviews.stream().anyMatch(r -> r.getVerdict() == ReviewVerdict.REJECTED)) {
            return ReviewVerdict.REJECTED;
        }
        if (reviews.stream().anyMatch(r -> r.getVerdict() == ReviewVerdict.APPROVED_WITH_RISK)) {
            return ReviewVerdict.APPROVED_WITH_RISK;
        }
        return reviews.isEmpty() ? ReviewVerdict.APPROVED : ReviewVerdict.APPROVED;
    }

    private static List<String> buildExecutiveSummary(List<FeatureReviewItem> reviews, ReviewVerdict overall) {
        List<String> summary = new ArrayList<>();
        summary.add("Overall PR test review verdict: " + overall + ".");
        summary.add(reviews.size() + " feature change(s) evaluated against smoke, contract, regression, and UI suites.");
        long missingContract = reviews.stream()
                .filter(r -> r.getMissingTests().stream().anyMatch(m -> m.contains("Contract")))
                .count();
        if (missingContract > 0) {
            summary.add(missingContract + " feature(s) lack contract test coverage.");
        }
        reviews.stream()
                .filter(r -> r.getVerdict() == ReviewVerdict.REJECTED)
                .limit(2)
                .forEach(r -> summary.add("REJECTED: " + r.getFeature().getFeatureName()
                        + " — " + r.getMissingTests().stream().findFirst().orElse("coverage gap")));
        return summary;
    }

    private static String buildPrSummary(List<String> changedFiles, List<ApiChange> apiChanges) {
        return changedFiles.size() + " changed file(s), " + apiChanges.size() + " API change(s) detected.";
    }

    private static String summarizeSources(List<String> changedFiles, List<ApiChange> apiChanges) {
        return "Changed files: " + changedFiles.size() + "; API changes: " + apiChanges.size()
                + "; Test sources: src/test/java";
    }

    public static void main(String[] args) {
        new TestReviewAgent().run();
    }
}
