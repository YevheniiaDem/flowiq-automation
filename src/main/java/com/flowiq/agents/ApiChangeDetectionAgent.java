package com.flowiq.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flowiq.agents.analyzer.ChangeAnalyzerPipeline;
import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.impact.ImpactMatrixBuilder;
import com.flowiq.agents.impact.TestImpactMapper;
import com.flowiq.agents.llm.LlmProvider;
import com.flowiq.agents.llm.LlmProviderFactory;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ImpactMatrixEntry;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.openapi.OpenApiFetcher;
import com.flowiq.agents.openapi.OpenApiSnapshotStore;
import com.flowiq.agents.report.ApiChangeReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Production-grade agent that detects backend API changes by comparing
 * the live OpenAPI specification against a stored snapshot, then maps
 * impacts to automated test suites.
 */
@Slf4j
public class ApiChangeDetectionAgent {

    private final AgentConfig agentConfig;
    private final ObjectMapper objectMapper;
    private final OpenApiFetcher openApiFetcher;
    private final OpenApiSnapshotStore snapshotStore;
    private final ChangeAnalyzerPipeline analyzerPipeline;
    private final TestImpactMapper testImpactMapper;
    private final ImpactMatrixBuilder impactMatrixBuilder;
    private final ApiChangeReportGenerator reportGenerator;
    private final LlmProvider llmProvider;

    public ApiChangeDetectionAgent() {
        this(ConfigFactory.create(AgentConfig.class));
    }

    public ApiChangeDetectionAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.openApiFetcher = new OpenApiFetcher(agentConfig, objectMapper);
        this.snapshotStore = new OpenApiSnapshotStore(agentConfig, objectMapper);
        this.analyzerPipeline = ChangeAnalyzerPipeline.defaultPipeline();
        this.testImpactMapper = new TestImpactMapper(agentConfig);
        this.impactMatrixBuilder = new ImpactMatrixBuilder(testImpactMapper);
        this.reportGenerator = new ApiChangeReportGenerator(agentConfig);
        this.llmProvider = LlmProviderFactory.create(agentConfig);
    }

    public AnalysisResult run() {
        log.info("Starting ApiChangeDetectionAgent");
        JsonNode currentSpec = openApiFetcher.fetchCurrentSpec();
        Optional<JsonNode> previousSnapshot = snapshotStore.loadPreviousSnapshot();

        boolean baselineMissing = previousSnapshot.isEmpty();
        List<ApiChange> changes = baselineMissing
                ? List.of()
                : analyzerPipeline.analyze(previousSnapshot.get(), currentSpec);

        Map<TestSuiteType, List<String>> affectedTests = testImpactMapper.mapChanges(changes);
        List<String> recommendedActions = testImpactMapper.recommendedActions(changes, affectedTests);
        RiskLevel riskLevel = baselineMissing ? RiskLevel.LOW : RiskLevel.fromChanges(changes);

        AnalysisResult preliminary = AnalysisResult.builder()
                .analyzedAt(Instant.now())
                .changes(changes)
                .riskLevel(riskLevel)
                .affectedTests(affectedTests)
                .recommendedActions(recommendedActions)
                .baselineMissing(baselineMissing)
                .build();

        String llmInsight = llmProvider.enrichAnalysis(preliminary).orElse(null);
        AnalysisResult result = AnalysisResult.builder()
                .analyzedAt(preliminary.getAnalyzedAt())
                .changes(changes)
                .riskLevel(riskLevel)
                .affectedTests(affectedTests)
                .recommendedActions(recommendedActions)
                .llmInsight(llmInsight)
                .baselineMissing(baselineMissing)
                .build();

        List<ImpactMatrixEntry> impactMatrix = impactMatrixBuilder.build(result);
        Path reportPath = reportGenerator.generate(result, impactMatrix);

        if (agentConfig.saveSnapshotOnRun() || baselineMissing) {
            snapshotStore.saveSnapshot(currentSpec);
        }

        log.info("Analysis complete. {} change(s), risk={}, report={}",
                changes.size(), riskLevel, reportPath.toAbsolutePath());

        if (agentConfig.failOnBreakingChanges() && changes.stream().anyMatch(ApiChange::isBreaking)) {
            throw new IllegalStateException("Breaking API changes detected. See " + reportPath.toAbsolutePath());
        }

        return result;
    }

    public static void main(String[] args) {
        ApiChangeDetectionAgent agent = new ApiChangeDetectionAgent();
        agent.run();
    }
}
