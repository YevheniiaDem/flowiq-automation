package com.flowiq.agents.architecture.report;

import com.flowiq.agents.architecture.config.ArchitectureDriftAgentConfig;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.ArchitectureDriftResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ArchitectureDriftReportGenerator {

    private final ArchitectureDriftAgentConfig config;

    public ArchitectureDriftReportGenerator(ArchitectureDriftAgentConfig config) {
        this.config = config;
    }

    public Path generate(ArchitectureDriftResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Architecture drift report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(ArchitectureDriftResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Architecture Drift Analysis\n\n");
        md.append("_Alignment check between FlowIQ architecture docs, OpenAPI, source code, and tests_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Architecture Health Score\n\n");
        md.append("### ").append(result.getArchitectureHealthScore()).append(" / 100\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Analysis completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Issues found | ").append(result.getIssuesFound()).append(" |\n");
        md.append("| Critical issues | ").append(result.getCriticalIssues()).append(" |\n");
        md.append("\n");

        if (result.getIssues().isEmpty()) {
            md.append("_No architecture drift detected._\n\n");
        } else {
            int index = 1;
            for (ArchitectureDriftIssue issue : result.getIssues()) {
                md.append("## Drift Issue ").append(index++).append("\n\n");
                appendField(md, "Issue", issue.getIssue() + " (`" + issue.getType() + "`)");
                appendField(md, "Severity", issue.getSeverity().name());
                appendField(md, "Location", issue.getLocation());
                appendField(md, "Recommendation", issue.getRecommendation());
                md.append("---\n\n");
            }
        }

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Drift Checks\n\n");
        md.append("| Check | Description |\n");
        md.append("|-------|-------------|\n");
        md.append("| Endpoint ↔ Docs | OpenAPI endpoints must appear in docs/ADR |\n");
        md.append("| Docs ↔ OpenAPI | Documented endpoints must exist in OpenAPI |\n");
        md.append("| Service ↔ Tests | API service clients require regression/smoke tests |\n");
        md.append("| Controller ↔ Contract | Controllers/API modules require contract tests |\n");
        md.append("| Page ↔ UI tests | Page objects require UI smoke coverage |\n");
        md.append("| DTO ↔ Schema | DTO models should have JSON schema definitions |\n");

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
