package com.flowiq.agents.report;

import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ImpactMatrixEntry;
import com.flowiq.agents.model.TestSuiteType;
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
public class ApiChangeReportGenerator {

    private final AgentConfig agentConfig;

    public ApiChangeReportGenerator(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    public Path generate(AnalysisResult result, List<ImpactMatrixEntry> impactMatrix) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result, impactMatrix));
            log.info("API change report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(AnalysisResult result, List<ImpactMatrixEntry> impactMatrix) {
        StringBuilder md = new StringBuilder();
        md.append("# API Change Report\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Changes\n\n");
        if (result.isBaselineMissing()) {
            md.append("_No previous snapshot found. Baseline established from current specification._\n\n");
        }
        if (result.getChanges().isEmpty()) {
            md.append("No changes detected.\n\n");
        } else {
            for (ApiChange change : result.getChanges()) {
                if (change.getType() == com.flowiq.agents.model.ChangeType.BREAKING_CHANGE) {
                    continue;
                }
                md.append("- **").append(change.getType()).append("**: ").append(change.getDescription());
                if (change.isBreaking()) {
                    md.append(" _(breaking)_");
                }
                md.append("\n");
            }
            md.append("\n");
        }

        md.append("## Risk Level\n\n");
        md.append("**").append(result.getRiskLevel()).append("**\n\n");

        md.append("## Affected Tests\n\n");
        Map<TestSuiteType, List<String>> affected = result.getAffectedTests();
        for (TestSuiteType suite : TestSuiteType.values()) {
            List<String> tests = affected.getOrDefault(suite, List.of());
            md.append("### ").append(suite.displayName()).append("\n\n");
            if (tests.isEmpty()) {
                md.append("_None_\n\n");
            } else {
                tests.forEach(test -> md.append("- ").append(test).append("\n"));
                md.append("\n");
            }
        }

        md.append("## Impact Matrix\n\n");
        if (impactMatrix.isEmpty()) {
            md.append("_No endpoint-level impacts._\n\n");
        } else {
            md.append("| Endpoint | Method | Risk | Contract | Smoke | Regression | UI |\n");
            md.append("|----------|--------|------|----------|-------|------------|----|\n");
            for (ImpactMatrixEntry entry : impactMatrix) {
                md.append("| ").append(nullSafe(entry.getApiPath()))
                        .append(" | ").append(nullSafe(entry.getHttpMethod()))
                        .append(" | ").append(entry.getRiskLevel())
                        .append(" | ").append(join(entry.getContractTests()))
                        .append(" | ").append(join(entry.getSmokeTests()))
                        .append(" | ").append(join(entry.getRegressionTests()))
                        .append(" | ").append(join(entry.getUiTests()))
                        .append(" |\n");
            }
            md.append("\n");
        }

        md.append("## Recommended Actions\n\n");
        if (result.getRecommendedActions().isEmpty()) {
            md.append("_No actions required._\n\n");
        } else {
            result.getRecommendedActions().forEach(action -> md.append("- ").append(action).append("\n"));
            md.append("\n");
        }

        if (result.getLlmInsight() != null && !result.getLlmInsight().isBlank()) {
            md.append("## AI Insight\n\n");
            md.append(result.getLlmInsight()).append("\n\n");
        }

        return md.toString();
    }

    private static String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "-";
        }
        return values.stream().collect(Collectors.joining(", "));
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Path resolveReportPath() {
        Path path = Paths.get(agentConfig.reportOutputPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
