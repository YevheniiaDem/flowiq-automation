package com.flowiq.agents.maintenance.report;

import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceHealthCategory;
import com.flowiq.agents.maintenance.model.TestMaintenanceResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class TestMaintenanceReportGenerator {

    private final TestMaintenanceAgentConfig config;

    public TestMaintenanceReportGenerator(TestMaintenanceAgentConfig config) {
        this.config = config;
    }

    public Path generate(TestMaintenanceResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Test maintenance report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(TestMaintenanceResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Test Maintenance Report\n\n");
        md.append("_Automated detection of automation framework technical debt and quality degradation_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Automation Health Score\n\n");
        md.append("### ").append(result.getAutomationHealthScore()).append("/100 - ")
                .append(formatCategory(result.getHealthCategory())).append("\n\n");

        md.append("## Technical Debt Summary\n\n");
        if (result.getTechnicalDebtSummary().isEmpty()) {
            md.append("- No significant technical debt detected.\n");
        } else {
            result.getTechnicalDebtSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Total findings | ").append(result.getFindingsCount()).append(" |\n");
        md.append("| Dead components | ").append(result.getDeadComponents()).append(" |\n");
        md.append("| Duplicate components | ").append(result.getDuplicateComponents()).append(" |\n");
        md.append("| Flaky candidates | ").append(result.getFlakyCandidates()).append(" |\n");
        md.append("\n");

        appendFindingsSection(md, "## Dead Components",
                filterType(result.getFindings(), MaintenanceFindingType.DEAD_CODE),
                "_No dead components detected._");

        appendFindingsSection(md, "## Duplicate Components",
                filterType(result.getFindings(), MaintenanceFindingType.DUPLICATE),
                "_No duplicate components detected._");

        appendFindingsSection(md, "## Flaky Candidates",
                filterType(result.getFindings(), MaintenanceFindingType.FLAKY),
                "_No flaky candidates detected in Allure history._");

        md.append("## Refactoring Recommendations\n\n");
        if (result.getRefactoringRecommendations().isEmpty()) {
            md.append("_No refactoring recommendations._\n\n");
        } else {
            int index = 1;
            for (String recommendation : result.getRefactoringRecommendations()) {
                md.append(index++).append(". ").append(recommendation).append("\n");
            }
            md.append("\n");
        }

        md.append("## Top Priority Fixes\n\n");
        if (result.getTopPriorityFixes().isEmpty()) {
            md.append("_No priority fixes required._\n\n");
        } else {
            int index = 1;
            for (String fix : result.getTopPriorityFixes()) {
                md.append(index++).append(". ").append(fix).append("\n");
            }
            md.append("\n");
        }

        md.append("## All Findings\n\n");
        appendFindingsTable(md, result.getFindings());

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        return md.toString();
    }

    private static void appendFindingsSection(StringBuilder md,
                                              String title,
                                              List<MaintenanceFinding> findings,
                                              String emptyMessage) {
        md.append(title).append("\n\n");
        if (findings.isEmpty()) {
            md.append(emptyMessage).append("\n\n");
            return;
        }
        appendFindingsTable(md, findings);
    }

    private static void appendFindingsTable(StringBuilder md, List<MaintenanceFinding> findings) {
        if (findings.isEmpty()) {
            return;
        }
        md.append("| Severity | Type | Issue | Location | Recommendation |\n");
        md.append("|----------|------|-------|----------|----------------|\n");
        for (MaintenanceFinding finding : findings) {
            md.append("| ").append(finding.getSeverity()).append(" | ")
                    .append(finding.getType().name().replace('_', ' ')).append(" | ")
                    .append(escapePipe(finding.getTitle())).append(" | ")
                    .append(escapePipe(finding.getLocation())).append(" | ")
                    .append(escapePipe(finding.getRecommendation())).append(" |\n");
        }
        md.append("\n");
    }

    private static List<MaintenanceFinding> filterType(List<MaintenanceFinding> findings,
                                                       MaintenanceFindingType type) {
        return findings.stream().filter(f -> f.getType() == type).toList();
    }

    private static String formatCategory(MaintenanceHealthCategory category) {
        return category.name().replace('_', ' ');
    }

    private static String escapePipe(String value) {
        return value == null ? "" : value.replace("|", "\\|");
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
