package com.flowiq.agents.regressionrisk.report;

import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.AffectedTests;
import com.flowiq.agents.regressionrisk.model.ModuleChangeImpact;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RegressionRiskReportGenerator {

    private final RegressionRiskAgentConfig config;

    public RegressionRiskReportGenerator(RegressionRiskAgentConfig config) {
        this.config = config;
    }

    public Path generate(RiskBasedRegressionResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Regression risk report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(RiskBasedRegressionResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Risk-Based Regression Plan\n\n");
        md.append("_Minimal regression scope recommendation for the upcoming release_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Final Recommendation\n\n");
        md.append("### ").append(formatRecommendation(result.getRecommendation())).append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Analysis completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Modules analyzed | ").append(result.getModulesAnalyzed()).append(" |\n");
        md.append("| Selected test classes | ").append(result.getTotalSelectedTestClasses()).append(" |\n");
        md.append("| Estimated execution time | ").append(result.getEstimatedTotalExecutionMinutes())
                .append(" min |\n");
        md.append("\n");

        if (result.getModulePlans().isEmpty()) {
            md.append("_No module changes detected — default to smoke-only validation._\n\n");
        } else {
            int index = 1;
            for (ModuleChangeImpact plan : result.getModulePlans()) {
                md.append("## Module Plan ").append(index++).append("\n\n");
                appendField(md, "Changed Module", plan.getModule()
                        + formatChangeHints(plan.isBackendChanged(), plan.isFrontendChanged()));
                appendField(md, "Risk", plan.getRisk().name());
                appendField(md, "Affected Tests", formatTests(plan.getAllAffectedTests()));
                appendField(md, "Recommended Regression Scope",
                        plan.getRecommendedRegressionScope() + "\n\nSelected: "
                                + formatTests(plan.getSelectedTests()));
                appendField(md, "Estimated Execution Time", plan.getEstimatedExecutionMinutes() + " min");
                md.append("---\n\n");
            }
        }

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Scope Guidelines\n\n");
        md.append("| Recommendation | When to use |\n");
        md.append("|----------------|-------------|\n");
        md.append("| FULL REGRESSION | Critical modules or breaking API changes |\n");
        md.append("| PARTIAL REGRESSION | Targeted high/medium risk modules only |\n");
        md.append("| SMOKE ONLY | Low-impact changes with minimal blast radius |\n");

        return md.toString();
    }

    private static String formatRecommendation(RegressionScopeRecommendation recommendation) {
        return switch (recommendation) {
            case FULL_REGRESSION -> "FULL REGRESSION";
            case PARTIAL_REGRESSION -> "PARTIAL REGRESSION";
            case SMOKE_ONLY -> "SMOKE ONLY";
        };
    }

    private static String formatChangeHints(boolean backend, boolean frontend) {
        List<String> hints = new ArrayList<>();
        if (backend) {
            hints.add("backend");
        }
        if (frontend) {
            hints.add("frontend");
        }
        return hints.isEmpty() ? "" : " (" + String.join(" + ", hints) + ")";
    }

    private static String formatTests(AffectedTests tests) {
        StringBuilder sb = new StringBuilder();
        appendSuite(sb, "Smoke", tests.getSmokeTests());
        appendSuite(sb, "Contract", tests.getContractTests());
        appendSuite(sb, "Regression", tests.getRegressionTests());
        appendSuite(sb, "UI", tests.getUiTests());
        return sb.isEmpty() ? "—" : sb.toString().trim();
    }

    private static void appendSuite(StringBuilder sb, String label, List<String> classes) {
        if (!classes.isEmpty()) {
            sb.append("- **").append(label).append("**: ")
                    .append(String.join(", ", classes)).append("\n");
        }
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
