package com.flowiq.agents.regressionrisk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.analyzer.ChangeAnalyzerPipeline;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiSnapshotStore;
import com.flowiq.agents.regressionrisk.analyzer.BusinessCriticalityAnalyzer;
import com.flowiq.agents.regressionrisk.analyzer.ChangeImpactAnalyzer;
import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.ModuleChangeImpact;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.ReleaseChangeContext;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.regressionrisk.report.RegressionRiskReportGenerator;
import com.flowiq.agents.regressionrisk.scanner.ReleaseChangeScanner;
import com.flowiq.agents.regressionrisk.scorer.RegressionRiskScorer;
import com.flowiq.agents.regressionrisk.selector.RegressionSelector;
import com.flowiq.agents.gap.model.GapSeverity;
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
import java.util.Map;

/**
 * AI agent that determines the minimal regression test set for a release
 * based on git diff, OpenAPI changes, and backend/frontend module impact.
 */
@Slf4j
public class RiskBasedRegressionAgent {

    private final RegressionRiskAgentConfig config;
    private final ReleaseChangeScanner changeScanner;
    private final ChangeImpactAnalyzer changeImpactAnalyzer;
    private final BusinessCriticalityAnalyzer criticalityAnalyzer;
    private final RegressionSelector regressionSelector;
    private final RegressionRiskScorer riskScorer;
    private final RegressionRiskReportGenerator reportGenerator;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final OpenApiSnapshotStore snapshotStore;
    private final ChangeAnalyzerPipeline changeAnalyzerPipeline;

    public RiskBasedRegressionAgent() {
        this(ConfigFactory.create(RegressionRiskAgentConfig.class));
    }

    public RiskBasedRegressionAgent(RegressionRiskAgentConfig config) {
        this.config = config;
        this.changeScanner = new ReleaseChangeScanner(config);
        this.changeImpactAnalyzer = new ChangeImpactAnalyzer();
        this.criticalityAnalyzer = new BusinessCriticalityAnalyzer();
        this.regressionSelector = new RegressionSelector(config);
        this.riskScorer = new RegressionRiskScorer(config);
        this.reportGenerator = new RegressionRiskReportGenerator(config);
        this.objectMapper = new ObjectMapper();
        AgentConfig agentConfig = ConfigFactory.create(AgentConfig.class);
        this.openApiFetcher = new OpenApiFetcher(agentConfig, objectMapper);
        this.snapshotStore = new OpenApiSnapshotStore(agentConfig, objectMapper);
        this.changeAnalyzerPipeline = ChangeAnalyzerPipeline.defaultPipeline();
    }

    public RiskBasedRegressionResult run() {
        log.info("Starting RiskBasedRegressionAgent");
        List<String> changedFiles = changeScanner.loadChangedFiles();
        List<ApiChange> apiChanges = detectApiChanges();
        ReleaseChangeContext context = changeScanner.scan(changedFiles, apiChanges);
        return run(context, summarizeSources(changedFiles, apiChanges));
    }

    public RiskBasedRegressionResult run(ReleaseChangeContext context, String dataSourcesSummary) {
        Map<String, ChangeImpactAnalyzer.ModuleImpactDraft> drafts =
                changeImpactAnalyzer.analyze(context);

        List<ModuleChangeImpact> modulePlans = new ArrayList<>();
        for (ChangeImpactAnalyzer.ModuleImpactDraft draft : drafts.values()) {
            GapSeverity risk = criticalityAnalyzer.analyze(
                    draft.getModule(),
                    draft.isBackendChanged(),
                    draft.isFrontendChanged(),
                    draft.getApiChanges());
            modulePlans.add(regressionSelector.select(draft, risk));
        }
        modulePlans.sort(Comparator.comparing((ModuleChangeImpact p) -> p.getRisk().ordinal())
                .thenComparing(ModuleChangeImpact::getModule));

        RegressionScopeRecommendation recommendation = riskScorer.recommend(modulePlans, context.getApiChanges());
        int totalMinutes = riskScorer.totalExecutionMinutes(modulePlans);
        int totalTests = riskScorer.totalSelectedTestClasses(modulePlans);

        var resultBuilder = RiskBasedRegressionResult.builder()
                .analyzedAt(Instant.now())
                .recommendation(recommendation)
                .modulesAnalyzed(modulePlans.size())
                .totalSelectedTestClasses(totalTests)
                .estimatedTotalExecutionMinutes(totalMinutes)
                .dataSourcesSummary(dataSourcesSummary);
        modulePlans.forEach(resultBuilder::modulePlan);
        buildExecutiveSummary(modulePlans, recommendation, totalMinutes).forEach(resultBuilder::summaryLine);
        RiskBasedRegressionResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Regression risk analysis complete. Recommendation={}, modules={}, report={}",
                recommendation, modulePlans.size(), reportPath.toAbsolutePath());
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

    private static List<String> buildExecutiveSummary(List<ModuleChangeImpact> plans,
                                                      RegressionScopeRecommendation recommendation,
                                                      int totalMinutes) {
        List<String> summary = new ArrayList<>();
        summary.add("Recommended release validation: " + recommendation.name().replace('_', ' ') + ".");
        summary.add(plans.size() + " module(s) impacted with risk-based test selection.");
        summary.add("Estimated targeted regression time: " + totalMinutes + " minutes.");
        plans.stream()
                .filter(p -> p.getRisk() == GapSeverity.CRITICAL || p.getRisk() == GapSeverity.HIGH)
                .limit(3)
                .forEach(p -> summary.add(p.getRisk() + " — " + p.getModule() + ": "
                        + p.getSelectedTests().totalTestClasses() + " test class(es)"));
        return summary;
    }

    private static String summarizeSources(List<String> changedFiles, List<ApiChange> apiChanges) {
        return "Git/manifest files: " + changedFiles.size()
                + "; OpenAPI changes: " + apiChanges.size()
                + "; Test mapping: test-impact-mapping.properties";
    }

    public static void main(String[] args) {
        new RiskBasedRegressionAgent().run();
    }
}
