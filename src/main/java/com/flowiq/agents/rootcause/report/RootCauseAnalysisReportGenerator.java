package com.flowiq.agents.rootcause.report;

import com.flowiq.agents.rootcause.config.RootCauseAgentConfig;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class RootCauseAnalysisReportGenerator {

    private final RootCauseAgentConfig config;

    public RootCauseAnalysisReportGenerator(RootCauseAgentConfig config) {
        this.config = config;
    }

    public Path generate(RootCauseAnalysisResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Root cause analysis report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(RootCauseAnalysisResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Root Cause Analysis\n\n");
        md.append("_Automated failure diagnosis from Allure, Surefire, artifacts, and backend logs_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Analysis completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Failures analyzed | ").append(result.getFailuresAnalyzed()).append(" |\n");
        md.append("| High-confidence findings (≥70%) | ").append(result.getHighConfidenceFindings()).append(" |\n");
        md.append("\n");

        if (result.getFindings().isEmpty()) {
            md.append("_No failed tests detected in the analyzed execution artifacts._\n\n");
        } else {
            int index = 1;
            for (RootCauseFinding finding : result.getFindings()) {
                md.append("## Failure ").append(index++).append("\n\n");
                appendField(md, "Failed Test", finding.getFailedTest());
                appendField(md, "Symptoms", finding.getSymptoms());
                appendField(md, "Most Probable Root Cause", finding.getMostProbableRootCause().name());
                appendField(md, "Confidence", finding.getConfidence() + "%");
                appendField(md, "Evidence",
                        finding.getEvidence().isEmpty()
                                ? "—"
                                : String.join("\n", finding.getEvidence().stream()
                                .map(e -> "- " + e)
                                .toList()));
                appendField(md, "Recommended Fix", finding.getRecommendedFix());
                md.append("---\n\n");
            }
        }

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Root Cause Categories\n\n");
        md.append("| Category | Typical signal |\n");
        md.append("|----------|----------------|\n");
        md.append("| BACKEND_BUG | HTTP 5xx, server exceptions in logs |\n");
        md.append("| UI_BUG | Playwright locator/timeout failures |\n");
        md.append("| TEST_BUG | Incorrect test setup or assertion |\n");
        md.append("| NETWORK | Connection/SSL/gateway errors |\n");
        md.append("| AUTH | 401/403, token/credential issues |\n");
        md.append("| DATA | Fixture/DB constraint/assertion mismatch |\n");
        md.append("| ENVIRONMENT | Missing service, port/env misconfiguration |\n");

        return md.toString();
    }

    private static void appendField(StringBuilder md, String label, String value) {
        md.append("**").append(label).append("**\n\n");
        md.append(value).append("\n\n");
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
