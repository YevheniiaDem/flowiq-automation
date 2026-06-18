package com.flowiq.agents.traceability.report;

import com.flowiq.agents.traceability.config.TraceabilityAgentConfig;
import com.flowiq.agents.traceability.model.FeatureTraceabilityRow;
import com.flowiq.agents.traceability.model.TraceabilityAnalysisResult;
import com.flowiq.agents.traceability.model.TraceabilityIssue;
import com.flowiq.agents.traceability.model.TraceabilityIssueType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TraceabilityMatrixReportGenerator {

    private final TraceabilityAgentConfig config;

    public TraceabilityMatrixReportGenerator(TraceabilityAgentConfig config) {
        this.config = config;
    }

    public Path generate(TraceabilityAnalysisResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Traceability matrix written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(TraceabilityAnalysisResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Requirements Traceability Matrix\n\n");
        md.append("_Business features mapped to OpenAPI endpoints and automated test suites_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Executive Summary\n\n");
        md.append("_For architects and QA managers_\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Traceability analysis completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Features in matrix | ").append(result.getFeatureCount()).append(" |\n");
        md.append("| Documented in docs/ | ").append(result.getDocumentedFeatureCount()).append(" |\n");
        md.append("| OpenAPI endpoints | ").append(result.getOpenApiEndpointCount()).append(" |\n");
        md.append("| **Overall coverage** | **").append(formatPercent(result.getOverallCoveragePercent()))
                .append("%** |\n");
        md.append("\n");

        md.append("## Traceability Matrix\n\n");
        md.append("| Feature | Endpoint | Smoke | Regression | Contract | UI | Coverage % |\n");
        md.append("|---------|----------|:-----:|:----------:|:--------:|:--:|------------|\n");
        for (FeatureTraceabilityRow row : result.getMatrix()) {
            md.append("| **").append(row.getFeatureName()).append("**");
            md.append(" | ").append(escapePipe(row.getEndpointsSummary()));
            md.append(" | ").append(mark(row.isSmokeCovered()));
            md.append(" | ").append(mark(row.isRegressionCovered()));
            md.append(" | ").append(mark(row.isContractCovered()));
            md.append(" | ").append(mark(row.isUiCovered()));
            md.append(" | ").append(formatPercent(row.getCoveragePercent())).append("%");
            if (row.isHighRisk()) {
                md.append(" ⚠");
            }
            md.append(" |\n");
        }
        md.append("\n");

        md.append("### Test Class References\n\n");
        for (FeatureTraceabilityRow row : result.getMatrix()) {
            md.append("#### ").append(row.getFeatureName()).append(" (`").append(row.getModule()).append("`)\n\n");
            md.append("| Suite | Test Classes |\n");
            md.append("|-------|-------------|\n");
            md.append("| Smoke | ").append(row.getSmokeTests()).append(" |\n");
            md.append("| Regression | ").append(row.getRegressionTests()).append(" |\n");
            md.append("| Contract | ").append(row.getContractTests()).append(" |\n");
            md.append("| UI | ").append(row.getUiTests()).append(" |\n");
            if (!row.getDocSources().isEmpty()) {
                md.append("| Docs | ").append(String.join(", ", row.getDocSources())).append(" |\n");
            }
            md.append("\n");
        }

        appendIssueSection(md, "Missing Coverage", result.getIssues(), TraceabilityIssueType.MISSING_COVERAGE);
        appendIssueSection(md, "Broken Traceability", result.getIssues(), TraceabilityIssueType.BROKEN_TRACEABILITY);
        appendIssueSection(md, "High-Risk Features", result.getIssues(), TraceabilityIssueType.HIGH_RISK);

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Recommendations\n\n");
        md.append("1. Close **missing coverage** gaps before major releases, starting with CRITICAL/HIGH business modules.\n");
        md.append("2. Fix **broken traceability** links between docs/, OpenAPI, and test classes.\n");
        md.append("3. Prioritize **high-risk features** below ").append(config.highRiskCoverageThreshold())
                .append("% coverage in sprint planning.\n");
        md.append("4. Re-run `RequirementsTraceabilityAgent` after adding endpoints, tests, or documentation.\n\n");

        return md.toString();
    }

    private static void appendIssueSection(StringBuilder md, String title,
                                           List<TraceabilityIssue> allIssues,
                                           TraceabilityIssueType type) {
        List<TraceabilityIssue> issues = allIssues.stream()
                .filter(i -> i.getType() == type)
                .toList();
        md.append("## ").append(title).append("\n\n");
        if (issues.isEmpty()) {
            md.append("_None identified._\n\n");
            return;
        }
        Map<String, Long> byModule = issues.stream()
                .collect(Collectors.groupingBy(TraceabilityIssue::getModule, Collectors.counting()));
        md.append("**").append(issues.size()).append(" issue(s)** across ")
                .append(byModule.size()).append(" module(s).\n\n");
        md.append("| Severity | Feature | Module | Description |\n");
        md.append("|----------|---------|--------|-------------|\n");
        for (TraceabilityIssue issue : issues) {
            md.append("| ").append(issue.getSeverity())
                    .append(" | ").append(issue.getFeatureName())
                    .append(" | `").append(issue.getModule()).append("`")
                    .append(" | ").append(escapePipe(issue.getDescription()))
                    .append(" |\n");
        }
        md.append("\n");
    }

    private static String mark(boolean covered) {
        return covered ? "✓" : "✗";
    }

    private static String formatPercent(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static String escapePipe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|");
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
