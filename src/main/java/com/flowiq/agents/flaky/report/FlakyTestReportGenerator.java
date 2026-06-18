package com.flowiq.agents.flaky.report;

import com.flowiq.agents.flaky.config.FlakyTestAgentConfig;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.flaky.model.FlakyTestFinding;
import com.flowiq.agents.flaky.model.RootCauseType;
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
public class FlakyTestReportGenerator {

    private final FlakyTestAgentConfig config;

    public FlakyTestReportGenerator(FlakyTestAgentConfig config) {
        this.config = config;
    }

    public Path generate(FlakyInvestigationResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Flaky test report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(FlakyInvestigationResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Flaky Test Investigation Report\n\n");
        md.append("_Prepared for QA leadership review_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Analysis completed across ").append(result.getTotalExecutionsAnalyzed())
                    .append(" test executions (").append(result.getUniqueTests()).append(" unique tests).\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("## Portfolio Metrics\n\n");
        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Executions analyzed | ").append(result.getTotalExecutionsAnalyzed()).append(" |\n");
        md.append("| Unique tests | ").append(result.getUniqueTests()).append(" |\n");
        md.append("| **Pass rate** | **").append(fmt(result.getPortfolioPassRate())).append("%** |\n");
        md.append("| **Failure rate** | **").append(fmt(result.getPortfolioFailureRate())).append("%** |\n");
        md.append("| **Flakiness %** | **").append(fmt(result.getPortfolioFlakinessPercent())).append("%** |\n");
        md.append("| Flaky tests detected | ").append(result.getFlakyTestCount()).append(" |\n");
        md.append("\n");

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Top ").append(result.getTopUnstableTests().size())
                .append(" Unstable Tests\n\n");
        if (result.getTopUnstableTests().isEmpty()) {
            md.append("_No unstable tests met the minimum run threshold._\n\n");
        } else {
            md.append("| Rank | Test | Suite | Pass % | Fail % | Flaky % | Priority | Root Cause |\n");
            md.append("|------|------|-------|--------|--------|---------|----------|------------|\n");
            int rank = 1;
            for (FlakyTestFinding finding : result.getTopUnstableTests()) {
                var m = finding.getMetrics();
                md.append("| ").append(rank++)
                        .append(" | `").append(shortName(m.getTestKey())).append("`")
                        .append(" | ").append(m.getSuite())
                        .append(" | ").append(fmt(m.getPassRate()))
                        .append(" | ").append(fmt(m.getFailureRate()))
                        .append(" | ").append(fmt(m.getFlakinessPercent()))
                        .append(" | **").append(fmt(finding.getPriorityScore())).append("**")
                        .append(" | ").append(finding.getPrimaryRootCause().getType())
                        .append(" |\n");
            }
            md.append("\n");

            md.append("## Root Cause Hypotheses & Recommended Fixes\n\n");
            rank = 1;
            for (FlakyTestFinding finding : result.getTopUnstableTests()) {
                md.append("### ").append(rank++).append(". `")
                        .append(shortName(finding.getMetrics().getTestKey())).append("`\n\n");
                md.append("- **Priority score:** ").append(fmt(finding.getPriorityScore())).append("\n");
                md.append("- **Pass / Fail / Runs:** ")
                        .append(finding.getMetrics().getPassCount()).append(" / ")
                        .append(finding.getMetrics().getFailCount() + finding.getMetrics().getBrokenCount())
                        .append(" / ").append(finding.getMetrics().getTotalRuns()).append("\n");
                md.append("- **Primary hypothesis:** ").append(finding.getPrimaryRootCause().getType())
                        .append(" — ").append(finding.getPrimaryRootCause().getDescription()).append("\n");
                if (!finding.getAlternateCauses().isEmpty()) {
                    md.append("- **Alternate hypotheses:** ");
                    md.append(finding.getAlternateCauses().stream()
                            .map(h -> h.getType().name())
                            .collect(Collectors.joining(", ")));
                    md.append("\n");
                }
                if (finding.getLastFailureMessage() != null && !finding.getLastFailureMessage().isBlank()) {
                    md.append("- **Last failure:** `")
                            .append(truncate(finding.getLastFailureMessage(), 120)).append("`\n");
                }
                md.append("- **Recommended fix:** ").append(finding.getRecommendedFix()).append("\n\n");
            }
        }

        md.append("## Root Cause Distribution\n\n");
        Map<RootCauseType, Long> distribution = result.getTopUnstableTests().stream()
                .collect(Collectors.groupingBy(f -> f.getPrimaryRootCause().getType(), Collectors.counting()));
        if (distribution.isEmpty()) {
            md.append("_No root causes classified._\n\n");
        } else {
            distribution.entrySet().stream()
                    .sorted(Map.Entry.<RootCauseType, Long>comparingByValue().reversed())
                    .forEach(e -> md.append("- **").append(e.getKey()).append(":** ").append(e.getValue()).append("\n"));
            md.append("\n");
        }

        md.append("## Recommended Actions for Leadership\n\n");
        md.append("1. **Stabilize top 5 by priority score** before expanding suite coverage.\n");
        md.append("2. **Quarantine** tests with flakiness > 40% from PR gates; keep in nightly only.\n");
        md.append("3. **Track weekly** pass rate and flakiness % trend from this report.\n");
        md.append("4. **Investigate environment** when NETWORK or BACKEND root causes dominate.\n");
        md.append("5. **Assign owners** per suite (UI → frontend QA, API → backend QA).\n\n");

        return md.toString();
    }

    private static String shortName(String testKey) {
        if (testKey == null) return "unknown";
        int dot = testKey.lastIndexOf('.');
        return dot >= 0 ? testKey.substring(dot + 1) : testKey;
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private static String fmt(double value) {
        return String.format("%.1f", value);
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
