package com.flowiq.agents.orchestrator.report;

import com.flowiq.agents.orchestrator.config.QualityIntelligenceConfig;
import com.flowiq.agents.orchestrator.model.QualityAgentRunResult;
import com.flowiq.agents.orchestrator.model.QualityDimensionSummary;
import com.flowiq.agents.orchestrator.model.QualityIntelligenceResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class QualityIntelligenceReportGenerator {

    private final QualityIntelligenceConfig config;

    public QualityIntelligenceReportGenerator(QualityIntelligenceConfig config) {
        this.config = config;
    }

    public Path generate(QualityIntelligenceResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Quality intelligence report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(QualityIntelligenceResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Quality Intelligence Executive Report\n\n");
        md.append("_Unified platform quality assessment across all FlowIQ AI agents_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Final Quality Score\n\n");
        md.append("### ").append(result.getQualityScore()).append(" / 100 — ")
                .append(formatCategory(result.getCategory().name())).append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Orchestration completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Agents succeeded | ").append(result.getAgentsSucceeded()).append(" |\n");
        md.append("| Agents failed | ").append(result.getAgentsFailed()).append(" |\n");
        md.append("| Dimensions assessed | ").append(result.getDimensions().size()).append(" |\n");
        md.append("\n");

        for (QualityDimensionSummary dimension : result.getDimensions()) {
            md.append("## ").append(dimension.getName()).append("\n\n");
            md.append("**Health score:** ").append(dimension.getHealthScore()).append("/100\n\n");
            if (!dimension.getContributingAgents().isEmpty()) {
                md.append("**Agents:** ").append(String.join(", ", dimension.getContributingAgents())).append("\n\n");
            }
            if (dimension.getHighlights().isEmpty()) {
                md.append("- No highlights captured.\n\n");
            } else {
                dimension.getHighlights().forEach(line -> md.append("- ").append(line).append("\n"));
                md.append("\n");
            }
        }

        md.append("## Agent Execution Log\n\n");
        md.append("| Agent | Status | Duration | Message |\n");
        md.append("|-------|--------|----------|--------|\n");
        for (QualityAgentRunResult run : result.getAgentRuns()) {
            md.append("| ").append(run.getAgentType()).append(" | ")
                    .append(run.isSuccess() ? "OK" : "FAILED").append(" | ")
                    .append(run.getDurationMs()).append(" ms | ")
                    .append(escape(run.getMessage())).append(" |\n");
        }
        md.append("\n");

        md.append("## Quality Categories\n\n");
        md.append("| Score | Category |\n");
        md.append("|-------|----------|\n");
        md.append("| 85-100 | EXCELLENT |\n");
        md.append("| 70-84 | GOOD |\n");
        md.append("| 50-69 | NEEDS ATTENTION |\n");
        md.append("| 0-49 | CRITICAL |\n");

        return md.toString();
    }

    private static String formatCategory(String category) {
        return category.replace('_', ' ');
    }

    private static String escape(String value) {
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
