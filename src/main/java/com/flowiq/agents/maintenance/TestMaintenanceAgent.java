package com.flowiq.agents.maintenance;

import com.flowiq.agents.maintenance.analyzers.MaintenanceAnalyzerPipeline;
import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceHealthCategory;
import com.flowiq.agents.maintenance.model.TestMaintenanceResult;
import com.flowiq.agents.maintenance.report.TestMaintenanceReportGenerator;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;
import com.flowiq.agents.maintenance.scanner.MaintenanceInventoryScanner;
import com.flowiq.agents.maintenance.scorer.MaintenanceHealthScorer;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Production-grade agent that detects automation framework technical debt,
 * dead components, locator quality issues, and flaky test candidates.
 */
@Slf4j
public class TestMaintenanceAgent {

    private final TestMaintenanceAgentConfig config;
    private final MaintenanceInventoryScanner inventoryScanner;
    private final MaintenanceAnalyzerPipeline analyzerPipeline;
    private final MaintenanceHealthScorer healthScorer;
    private final TestMaintenanceReportGenerator reportGenerator;

    public TestMaintenanceAgent() {
        this(ConfigFactory.create(TestMaintenanceAgentConfig.class));
    }

    public TestMaintenanceAgent(TestMaintenanceAgentConfig config) {
        this.config = config;
        this.inventoryScanner = new MaintenanceInventoryScanner(config);
        this.analyzerPipeline = new MaintenanceAnalyzerPipeline();
        this.healthScorer = new MaintenanceHealthScorer();
        this.reportGenerator = new TestMaintenanceReportGenerator(config);
    }

    public TestMaintenanceResult run() {
        log.info("Starting TestMaintenanceAgent");
        MaintenanceContext context = inventoryScanner.scan();
        return run(context);
    }

    public TestMaintenanceResult run(MaintenanceContext context) {
        List<MaintenanceFinding> findings = analyzerPipeline.analyze(context);
        int score = healthScorer.score(findings);
        MaintenanceHealthCategory category = healthScorer.categorize(score);

        int dead = countType(findings, MaintenanceFindingType.DEAD_CODE);
        int duplicate = countType(findings, MaintenanceFindingType.DUPLICATE);
        int flaky = countType(findings, MaintenanceFindingType.FLAKY);

        var resultBuilder = TestMaintenanceResult.builder()
                .analyzedAt(Instant.now())
                .automationHealthScore(score)
                .healthCategory(category)
                .findingsCount(findings.size())
                .deadComponents(dead)
                .duplicateComponents(duplicate)
                .flakyCandidates(flaky)
                .dataSourcesSummary(context.getDataSourcesSummary());
        findings.forEach(resultBuilder::finding);
        buildTechnicalDebtSummary(findings, score, category).forEach(resultBuilder::technicalDebtSummaryLine);
        buildRefactoringRecommendations(findings).forEach(resultBuilder::refactoringRecommendationLine);
        buildTopPriorityFixes(findings).forEach(resultBuilder::topPriorityFixLine);

        TestMaintenanceResult result = resultBuilder.build();
        Path reportPath = reportGenerator.generate(result);
        log.info("Test maintenance complete. Score={}, category={}, findings={}, report={}",
                score, category, findings.size(), reportPath.toAbsolutePath());
        return result;
    }

    private static int countType(List<MaintenanceFinding> findings, MaintenanceFindingType type) {
        return (int) findings.stream().filter(f -> f.getType() == type).count();
    }

    private static List<String> buildTechnicalDebtSummary(List<MaintenanceFinding> findings,
                                                          int score,
                                                          MaintenanceHealthCategory category) {
        List<String> summary = new ArrayList<>();
        summary.add("Automation health score: " + score + "/100 (" + category + ").");
        summary.add(findings.size() + " maintenance finding(s) across dead code, duplicates, locators, and complexity.");
        long critical = findings.stream().filter(f -> f.getSeverity().name().equals("CRITICAL")).count();
        if (critical > 0) {
            summary.add(critical + " critical issue(s) require immediate attention.");
        }
        return summary;
    }

    private static List<String> buildRefactoringRecommendations(List<MaintenanceFinding> findings) {
        return findings.stream()
                .filter(f -> f.getType() == MaintenanceFindingType.COMPLEXITY
                        || f.getType() == MaintenanceFindingType.LOCATOR_QUALITY)
                .sorted(Comparator.comparingInt(MaintenanceFinding::getPriorityRank))
                .map(MaintenanceFinding::getRecommendation)
                .distinct()
                .limit(10)
                .toList();
    }

    private static List<String> buildTopPriorityFixes(List<MaintenanceFinding> findings) {
        return findings.stream()
                .sorted(Comparator.comparingInt(MaintenanceFinding::getPriorityRank)
                        .thenComparing(f -> f.getSeverity().ordinal()))
                .map(f -> f.getTitle() + " - " + f.getLocation() + ": " + f.getRecommendation())
                .distinct()
                .limit(5)
                .toList();
    }

    public static void main(String[] args) {
        new TestMaintenanceAgent().run();
    }
}
