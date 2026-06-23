package com.flowiq.agents.factory.report;

import com.flowiq.agents.factory.config.FailureIntelligenceConfig;
import com.flowiq.agents.factory.model.FactoryAgentRunResult;
import com.flowiq.agents.factory.model.FailureIntelligenceResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class FailureIntelligenceReportGenerator {

    private final FailureIntelligenceConfig config;

    public FailureIntelligenceReportGenerator(FailureIntelligenceConfig config) {
        this.config = config;
    }

    public Path generate(FailureIntelligenceResult result) {
        Path outputPath = FactoryReportSupport.resolveReportPath(config.reportOutputPath());
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Failure intelligence report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(FailureIntelligenceResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Failure Intelligence Report\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Failure Intelligence Score\n\n");
        md.append("### ").append(result.getFailureIntelligenceScore()).append(" / 100\n\n");

        md.append("## Metrics\n\n");
        md.append("| Metric | Score |\n");
        md.append("|--------|-------|\n");
        md.append("| Flakiness | ").append(result.getFlakinessMetric()).append("/100 |\n");
        md.append("| Root Cause Confidence | ").append(result.getRootCauseConfidenceMetric()).append("/100 |\n");
        md.append("| Locator Recovery Potential | ").append(result.getLocatorRecoveryPotentialMetric()).append("/100 |\n\n");

        md.append("## Executive Summary\n\n");
        result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        md.append("\n");

        md.append("## Top Risks\n\n");
        if (result.getTopRisks().isEmpty()) {
            md.append("- No failure intelligence risks identified.\n\n");
        } else {
            result.getTopRisks().forEach(line -> md.append("- ").append(line).append("\n"));
            md.append("\n");
        }

        md.append("## Recommended Actions\n\n");
        result.getRecommendedActions().forEach(line -> md.append("- ").append(line).append("\n"));
        md.append("\n");

        md.append("## Agent Execution Log\n\n");
        md.append("| Agent | Status | Duration | Message |\n");
        md.append("|-------|--------|----------|--------|\n");
        for (FactoryAgentRunResult run : result.getAgentRuns()) {
            md.append("| ").append(run.getAgentType().name())
                    .append(" | ").append(run.isSuccess() ? "SUCCESS" : "FAILED")
                    .append(" | ").append(run.getDurationMs()).append(" ms")
                    .append(" | ").append(run.getMessage()).append(" |\n");
        }
        md.append("\n");
        return md.toString();
    }
}
