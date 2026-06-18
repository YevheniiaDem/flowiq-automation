package com.flowiq.agents.review.report;

import com.flowiq.agents.review.config.TestReviewAgentConfig;
import com.flowiq.agents.review.model.CoverageStatus;
import com.flowiq.agents.review.model.FeatureReviewItem;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ReviewCommentGenerator {

    private final TestReviewAgentConfig config;

    public ReviewCommentGenerator(TestReviewAgentConfig config) {
        this.config = config;
    }

    public Path generate(TestReviewResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Test review report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(TestReviewResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Pull Request Test Review\n\n");
        md.append("_Automated test coverage quality assessment for new functionality_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getReviewedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Overall Verdict\n\n");
        md.append("### ").append(formatVerdict(result.getOverallVerdict())).append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Review completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Features reviewed | ").append(result.getFeaturesReviewed()).append(" |\n");
        md.append("| Rejected | ").append(result.getRejectedCount()).append(" |\n");
        md.append("| Approved with risk | ").append(result.getApprovedWithRiskCount()).append(" |\n");
        md.append("\n");

        if (result.getPullRequestSummary() != null && !result.getPullRequestSummary().isBlank()) {
            md.append("## Pull Request Context\n\n");
            md.append(result.getPullRequestSummary()).append("\n\n");
        }

        if (result.getFeatures().isEmpty()) {
            md.append("_No feature changes detected for review._\n\n");
        } else {
            int index = 1;
            for (FeatureReviewItem item : result.getFeatures()) {
                md.append("## Feature Review ").append(index++).append("\n\n");
                appendField(md, "Feature", item.getFeature().getFeatureName()
                        + " (`" + item.getFeature().getChangeType() + "`)");
                appendField(md, "Changed Files",
                        item.getFeature().getChangedFiles().isEmpty()
                                ? "— (API/OpenAPI change)"
                                : String.join(", ", item.getFeature().getChangedFiles()));
                appendField(md, "Coverage Status", formatCoverage(item.getCoverageStatus(),
                        item.getFeature().getModule()));
                appendField(md, "Missing Tests",
                        item.getMissingTests().isEmpty() ? "None" : String.join("; ", item.getMissingTests()));
                appendField(md, "Risk", item.getRisk().name());
                appendField(md, "Recommendation", item.getRecommendation());
                md.append("**Verdict:** ").append(formatVerdict(item.getVerdict())).append("\n\n");
                md.append("---\n\n");
            }
        }

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Review Guidelines\n\n");
        md.append("1. **REJECTED** — mandatory contract/regression gaps on new endpoints.\n");
        md.append("2. **APPROVED WITH RISK** — merge allowed with documented test debt.\n");
        md.append("3. **APPROVED** — coverage meets quality gates.\n\n");

        return md.toString();
    }

    private String formatCoverage(CoverageStatus status, String module) {
        return String.format(
                "Smoke: %s | Regression: %s | Contract: %s | UI: %s | Positive: %s | Negative: %s | Auth: %s (module: %s)",
                mark(status.isSmokeCovered()),
                mark(status.isRegressionCovered()),
                mark(status.isContractCovered()),
                mark(status.isUiCovered()),
                mark(status.isPositiveCovered()),
                mark(status.isNegativeCovered()),
                mark(status.isAuthorizationCovered()),
                module);
    }

    private static String mark(boolean covered) {
        return covered ? "✓" : "✗";
    }

    private static String formatVerdict(ReviewVerdict verdict) {
        return switch (verdict) {
            case APPROVED -> "APPROVED";
            case APPROVED_WITH_RISK -> "APPROVED WITH RISK";
            case REJECTED -> "REJECTED";
        };
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
