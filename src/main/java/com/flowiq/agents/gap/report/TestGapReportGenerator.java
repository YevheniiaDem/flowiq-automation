package com.flowiq.agents.gap.report;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.model.ModuleCoverage;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.model.TestGapAnalysisResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TestGapReportGenerator {

    private final TestGapAgentConfig config;

    public TestGapReportGenerator(TestGapAgentConfig config) {
        this.config = config;
    }

    public Path generate(TestGapAnalysisResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Test gap analysis report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(TestGapAnalysisResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Test Gap Analysis\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Coverage %\n\n");
        md.append("**Overall coverage: ").append(String.format("%.1f", result.getOverallCoveragePercent()))
                .append("%**\n\n");
        md.append("| Module | Endpoints | Covered | Coverage % |\n");
        md.append("|--------|-----------|---------|------------|\n");
        for (ModuleCoverage module : result.getModules()) {
            md.append("| ").append(module.getModule())
                    .append(" | ").append(module.getTotalEndpoints())
                    .append(" | ").append(module.getCoveredEndpoints())
                    .append(" | ").append(String.format("%.1f", module.getCoveragePercent())).append("% |\n");
        }
        md.append("\n");

        md.append("## Module Risk\n\n");
        md.append("| Module | Business Impact | Open Gaps | Highest Severity |\n");
        md.append("|--------|-----------------|-----------|------------------|\n");
        for (ModuleCoverage module : result.getModules()) {
            GapSeverity highest = module.getGaps().stream()
                    .map(TestGap::getSeverity)
                    .min(Comparator.naturalOrder())
                    .orElse(GapSeverity.LOW);
            md.append("| ").append(module.getModule())
                    .append(" | ").append(module.getBusinessImpact())
                    .append(" | ").append(module.getGaps().size())
                    .append(" | ").append(highest).append(" |\n");
        }
        md.append("\n");

        md.append("## Endpoint Coverage Matrix\n\n");
        md.append("| Endpoint | Method | Contract | Smoke | Regression | UI | Negative | Auth |\n");
        md.append("|----------|--------|----------|-------|------------|----|---------:|-----:|\n");
        for (ModuleCoverage module : result.getModules()) {
            for (EndpointCoverage endpoint : module.getEndpoints()) {
                md.append("| ").append(endpoint.getPath())
                        .append(" | ").append(endpoint.getMethod())
                        .append(" | ").append(flag(endpoint.isContractCovered()))
                        .append(" | ").append(flag(endpoint.isSmokeCovered()))
                        .append(" | ").append(flag(endpoint.isRegressionCovered()))
                        .append(" | ").append(flag(endpoint.isUiCovered()))
                        .append(" | ").append(flag(endpoint.isNegativeCovered()))
                        .append(" | ").append(flag(endpoint.isAuthorizationCovered()))
                        .append(" |\n");
            }
        }
        md.append("\n");

        md.append("## Missing Tests\n\n");
        Map<GapSeverity, List<TestGap>> bySeverity = result.getGaps().stream()
                .collect(Collectors.groupingBy(TestGap::getSeverity));
        for (GapSeverity severity : GapSeverity.values()) {
            List<TestGap> gaps = bySeverity.getOrDefault(severity, List.of());
            if (gaps.isEmpty()) {
                continue;
            }
            md.append("### ").append(severity).append("\n\n");
            for (TestGap gap : gaps) {
                md.append("- **").append(gap.getType()).append("** [").append(gap.getModule()).append("]: ")
                        .append(gap.getDescription()).append("\n");
            }
            md.append("\n");
        }
        if (result.getGaps().isEmpty()) {
            md.append("_No coverage gaps detected._\n\n");
        }

        md.append("## Recommended New Tests\n\n");
        if (result.getRecommendedTests().isEmpty()) {
            md.append("_No recommendations — coverage is sufficient._\n\n");
        } else {
            result.getRecommendedTests().forEach(test -> md.append("- ").append(test).append("\n"));
            md.append("\n");
        }

        if (result.getLlmInsight() != null && !result.getLlmInsight().isBlank()) {
            md.append("## AI Insight\n\n");
            md.append(result.getLlmInsight()).append("\n\n");
        }

        return md.toString();
    }

    private static String flag(boolean value) {
        return value ? "Y" : "-";
    }

    private Path resolveReportPath() {
        Path path = Paths.get(config.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
