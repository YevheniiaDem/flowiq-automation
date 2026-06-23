package com.flowiq.agents.factory.report;

import com.flowiq.agents.factory.config.AiQualityFactoryConfig;
import com.flowiq.agents.factory.model.AiQualityFactoryResult;
import com.flowiq.agents.factory.model.FactoryDimensionSummary;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AiQualityFactoryReportGenerator {

    private final AiQualityFactoryConfig config;

    public AiQualityFactoryReportGenerator(AiQualityFactoryConfig config) {
        this.config = config;
    }

    public Path generate(AiQualityFactoryResult result) {
        Path outputPath = FactoryReportSupport.resolveReportPath(config.reportOutputPath());
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("AI Quality Factory report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(AiQualityFactoryResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# AI Quality Factory Report\n\n");
        md.append("_Hierarchical AI Quality Engineering Factory — executive dashboard_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Overall Score\n\n");
        md.append("### ").append(result.getOverallScore()).append(" / 100 — ")
                .append(FactoryReportSupport.formatLabel(result.getCategory().name())).append("\n\n");

        md.append("## Executive Summary\n\n");
        result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        md.append("\n");

        md.append("## Factory Dimensions\n\n");
        md.append("| Dimension | Score |\n");
        md.append("|-----------|-------|\n");
        for (FactoryDimensionSummary dimension : result.getDimensions()) {
            md.append("| ").append(dimension.getName()).append(" | ")
                    .append(dimension.getHealthScore()).append("/100 |\n");
        }
        md.append("\n");

        for (FactoryDimensionSummary dimension : result.getDimensions()) {
            md.append("## ").append(dimension.getName()).append("\n\n");
            md.append("**Score:** ").append(dimension.getHealthScore()).append("/100\n\n");
            dimension.getHighlights().forEach(line -> md.append("- ").append(line).append("\n"));
            md.append("\n");
        }

        md.append("## Top Risks\n\n");
        if (result.getTopRisks().isEmpty()) {
            md.append("- No top risks identified.\n\n");
        } else {
            result.getTopRisks().forEach(line -> md.append("- ").append(line).append("\n"));
            md.append("\n");
        }

        md.append("## Recommended Actions\n\n");
        result.getRecommendedActions().forEach(line -> md.append("- ").append(line).append("\n"));
        md.append("\n");

        if (result.getPrIntelligence() != null) {
            md.append("## PR Intelligence Summary\n\n");
            md.append("- PR Quality Score: ").append(result.getPrIntelligence().getPrQualityScore()).append("/100\n");
            md.append("- Verdict: ").append(FactoryReportSupport.formatLabel(result.getPrIntelligence().getVerdict().name())).append("\n\n");
        }
        if (result.getGovernanceIntelligence() != null) {
            md.append("## Governance Intelligence Summary\n\n");
            md.append("- Governance Health: ").append(result.getGovernanceIntelligence().getGovernanceHealthScore()).append("/100\n");
            md.append("- Category: ").append(result.getGovernanceIntelligence().getCategory().name()).append("\n\n");
        }
        if (result.getFailureIntelligence() != null) {
            md.append("## Failure Intelligence Summary\n\n");
            md.append("- Failure Intelligence Score: ").append(result.getFailureIntelligence().getFailureIntelligenceScore()).append("/100\n\n");
        }

        return md.toString();
    }
}
