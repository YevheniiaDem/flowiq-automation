package com.flowiq.agents.release.report;

import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.model.BlockedArea;
import com.flowiq.agents.release.model.CriticalFailure;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.SuiteExecutionSummary;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ReleaseReadinessReportGenerator {

    private final ReleaseRiskAgentConfig config;

    public ReleaseReadinessReportGenerator(ReleaseRiskAgentConfig config) {
        this.config = config;
    }

    public Path generate(ReleaseRiskAssessmentResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Release readiness report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(ReleaseRiskAssessmentResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Release Readiness Report\n\n");
        md.append("_Automated release risk assessment for QA leadership_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAssessedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Overall Score\n\n");
        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| **Release Risk Score** | **").append(formatScore(result.getReleaseRiskScore()))
                .append(" / 100** |\n");
        md.append("| **Risk Category** | **").append(result.getRiskCategory()).append("** |\n");
        md.append("| **Final Recommendation** | **").append(formatRecommendation(result.getRecommendation()))
                .append("** |\n");
        md.append("\n");

        md.append("### Score Breakdown\n\n");
        result.getScoreBreakdown().forEach((key, value) ->
                md.append("- **").append(key).append(":** ").append(formatScore(value)).append("\n"));
        md.append("\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Assessment completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("## Suite Results\n\n");
        appendSuiteTable(md, "Regression", result.getRegressionSummary());
        appendSuiteTable(md, "Smoke", result.getSmokeSummary());
        appendSuiteTable(md, "Contract", result.getContractSummary());
        md.append("\n");

        md.append("## Supporting Reports\n\n");
        md.append("### Flaky Tests\n\n");
        md.append("- ").append(result.getFlakyInsight().getSummary()).append("\n");
        if (!result.getFlakyInsight().getTopUnstableTests().isEmpty()) {
            md.append("- Top unstable: `")
                    .append(String.join("`, `", result.getFlakyInsight().getTopUnstableTests()))
                    .append("`\n");
        }
        md.append("\n");

        md.append("### API Changes\n\n");
        md.append("- ").append(result.getApiChangeInsight().getSummary()).append("\n");
        if (!result.getApiChangeInsight().getBreakingChangeDescriptions().isEmpty()) {
            md.append("\n**Breaking changes:**\n\n");
            result.getApiChangeInsight().getBreakingChangeDescriptions()
                    .forEach(c -> md.append("- ").append(c).append("\n"));
        }
        md.append("\n");

        md.append("## Critical Failures\n\n");
        if (result.getCriticalFailures().isEmpty()) {
            md.append("_No critical failures in regression, smoke, or contract suites._\n\n");
        } else {
            md.append("| Severity | Suite | Module | Test | Message |\n");
            md.append("|----------|-------|--------|------|----------|\n");
            for (CriticalFailure failure : result.getCriticalFailures()) {
                md.append("| ").append(failure.getSeverity())
                        .append(" | ").append(failure.getSuiteType())
                        .append(" | ").append(failure.getModule())
                        .append(" | `").append(failure.getMethodName()).append("`")
                        .append(" | ").append(escapePipe(failure.getMessage()))
                        .append(" |\n");
            }
            md.append("\n");
        }

        md.append("## Blocked Areas\n\n");
        if (result.getBlockedAreas().isEmpty()) {
            md.append("_No modules blocked._\n\n");
        } else {
            int index = 1;
            for (BlockedArea area : result.getBlockedAreas()) {
                md.append("### ").append(index++).append(". ").append(area.getModule()).append("\n\n");
                md.append("- **Failures:** ").append(area.getFailureCount()).append("\n");
                md.append("- **Reason:** ").append(area.getReason()).append("\n");
                md.append("- **Affected suites:** ").append(String.join(", ", area.getAffectedSuites())).append("\n");
                if (!area.getAffectedTests().isEmpty()) {
                    md.append("- **Sample tests:** `")
                            .append(String.join("`, `", area.getAffectedTests()))
                            .append("`\n");
                }
                md.append("\n");
            }
        }

        md.append("## Recommended Actions\n\n");
        int action = 1;
        for (String item : result.getRecommendedActions()) {
            md.append(action++).append(". ").append(item).append("\n");
        }
        md.append("\n");

        md.append("## Final Recommendation\n\n");
        md.append("### ").append(formatRecommendation(result.getRecommendation())).append("\n\n");
        md.append(recommendationNarrative(result)).append("\n\n");

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        return md.toString();
    }

    private static void appendSuiteTable(StringBuilder md, String label, SuiteExecutionSummary summary) {
        md.append("### ").append(label).append("\n\n");
        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Tests | ").append(summary.getTotalTests()).append(" |\n");
        md.append("| Passed | ").append(summary.getPassed()).append(" |\n");
        md.append("| Failed | ").append(summary.getFailed()).append(" |\n");
        md.append("| Broken | ").append(summary.getBroken()).append(" |\n");
        md.append("| Skipped | ").append(summary.getSkipped()).append(" |\n");
        md.append("| **Pass rate** | **").append(formatScore(summary.getPassRate())).append("%** |\n");
        md.append("\n");
    }

    private static String formatScore(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static String formatRecommendation(com.flowiq.agents.release.model.ReleaseRecommendation rec) {
        return switch (rec) {
            case APPROVE_RELEASE -> "APPROVE RELEASE";
            case APPROVE_WITH_RISK -> "APPROVE WITH RISK";
            case DO_NOT_RELEASE -> "DO NOT RELEASE";
        };
    }

    private static String recommendationNarrative(ReleaseRiskAssessmentResult result) {
        return switch (result.getRecommendation()) {
            case APPROVE_RELEASE ->
                    "All gate suites meet thresholds. Release risk is **"
                            + result.getRiskCategory() + "** (score "
                            + formatScore(result.getReleaseRiskScore()) + "). Proceed with release.";
            case APPROVE_WITH_RISK ->
                    "Release may proceed with documented risk acceptance. Address recommended actions "
                            + "and monitor affected modules post-deploy.";
            case DO_NOT_RELEASE ->
                    "Critical failures or gate violations detected. Resolve blockers and re-run "
                            + "regression, smoke, and contract suites before release.";
        };
    }

    private static String escapePipe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\n", " ");
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
