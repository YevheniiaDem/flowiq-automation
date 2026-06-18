package com.flowiq.agents.architecture;

import com.flowiq.agents.architecture.checker.ArchitectureDriftCheckerPipeline;
import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.inventory.ArchitectureInventoryLoader;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import com.flowiq.agents.architecture.model.DriftSeverity;
import com.flowiq.agents.architecture.report.ArchitectureDriftReportGenerator;
import com.flowiq.agents.architecture.scorer.ArchitectureHealthScorer;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * AI agent that detects drift between FlowIQ architecture documentation,
 * OpenAPI contracts, backend/frontend source structure, and automated tests.
 */
@Slf4j
public class ArchitectureDriftAgent {

    private final ArchitectureDriftAgentConfig config;
    private final ArchitectureInventoryLoader inventoryLoader;
    private final ArchitectureDriftCheckerPipeline checkerPipeline;
    private final ArchitectureHealthScorer healthScorer;
    private final ArchitectureDriftReportGenerator reportGenerator;

    public ArchitectureDriftAgent() {
        this(ConfigFactory.create(ArchitectureDriftAgentConfig.class));
    }

    public ArchitectureDriftAgent(ArchitectureDriftAgentConfig config) {
        this.config = config;
        this.inventoryLoader = new ArchitectureInventoryLoader(config);
        this.checkerPipeline = new ArchitectureDriftCheckerPipeline();
        this.healthScorer = new ArchitectureHealthScorer();
        this.reportGenerator = new ArchitectureDriftReportGenerator(config);
    }

    public ArchitectureDriftResult run() {
        log.info("Starting ArchitectureDriftAgent");
        ArchitectureContext context = inventoryLoader.load();
        return run(context, inventoryLoader.summarizeSources());
    }

    public ArchitectureDriftResult run(ArchitectureContext context, String dataSourcesSummary) {
        List<ArchitectureDriftIssue> issues = checkerPipeline.analyze(context);
        int healthScore = healthScorer.score(issues);
        int critical = healthScorer.countBySeverity(issues, DriftSeverity.CRITICAL);

        var resultBuilder = ArchitectureDriftResult.builder()
                .analyzedAt(Instant.now())
                .architectureHealthScore(healthScore)
                .issuesFound(issues.size())
                .criticalIssues(critical)
                .dataSourcesSummary(dataSourcesSummary);
        issues.forEach(resultBuilder::issue);
        buildExecutiveSummary(issues, healthScore).forEach(resultBuilder::summaryLine);
        ArchitectureDriftResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Architecture drift analysis complete. Health={}, issues={}, report={}",
                healthScore, issues.size(), reportPath.toAbsolutePath());
        return result;
    }

    private static List<String> buildExecutiveSummary(List<ArchitectureDriftIssue> issues, int healthScore) {
        List<String> summary = new ArrayList<>();
        summary.add("Architecture health score: " + healthScore + "/100.");
        summary.add(issues.size() + " drift issue(s) detected across docs, OpenAPI, source, and tests.");
        long endpointGaps = issues.stream()
                .filter(i -> i.getType().name().contains("ENDPOINT")
                        || i.getType().name().contains("DOCUMENTATION"))
                .count();
        if (endpointGaps > 0) {
            summary.add(endpointGaps + " documentation ↔ OpenAPI mismatch(es) require alignment.");
        }
        long testGaps = issues.stream()
                .filter(i -> i.getType().name().contains("TEST") || i.getType().name().contains("CONTRACT"))
                .count();
        if (testGaps > 0) {
            summary.add(testGaps + " test coverage gap(s) against architecture standards.");
        }
        return summary;
    }

    public static void main(String[] args) {
        new ArchitectureDriftAgent().run();
    }
}
