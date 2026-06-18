package com.flowiq.agents.prreview.report;

import com.flowiq.agents.prreview.config.PullRequestReviewAgentConfig;
import com.flowiq.agents.prreview.model.PrReviewArea;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewVerdict;
import com.flowiq.agents.prreview.model.PullRequestReviewResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class PullRequestReviewReportGenerator {

    private final PullRequestReviewAgentConfig config;

    public PullRequestReviewReportGenerator(PullRequestReviewAgentConfig config) {
        this.config = config;
    }

    public Path generate(PullRequestReviewResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("PR review report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(PullRequestReviewResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Pull Request Review\n\n");
        md.append("_Automated QA and Architecture review before test execution_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getReviewedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Review completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Verdict | ").append(formatVerdict(result.getVerdict())).append(" |\n");
        md.append("| Files changed | ").append(result.getChangedFiles().size()).append(" |\n");
        md.append("| Findings | ").append(result.getFindingsCount()).append(" |\n");
        md.append("| Critical | ").append(result.getCriticalFindings()).append(" |\n");
        md.append("| High | ").append(result.getHighFindings()).append(" |\n");
        md.append("\n");

        md.append("## Files Changed\n\n");
        if (result.getChangedFiles().isEmpty()) {
            md.append("_No changed files detected._\n\n");
        } else {
            result.getChangedFiles().forEach(file -> md.append("- `").append(file).append("`\n"));
            md.append("\n");
        }

        md.append("## Detected Risks\n\n");
        appendFindingsTable(md, result.getFindings(), null);

        md.append("## Missing Coverage\n\n");
        List<PrReviewFinding> coverageFindings = result.getFindings().stream()
                .filter(f -> f.getCategory() == PrReviewCategory.API_REVIEW
                        || f.getCategory() == PrReviewCategory.AUTOMATION_REVIEW)
                .filter(f -> f.getTitle().toLowerCase().contains("without"))
                .toList();
        if (coverageFindings.isEmpty()) {
            md.append("_No missing coverage gaps detected._\n\n");
        } else {
            appendFindingsTable(md, coverageFindings, null);
        }

        md.append("## Architecture Findings\n\n");
        List<PrReviewFinding> architectureFindings = result.getFindings().stream()
                .filter(f -> f.getArea() == PrReviewArea.ARCHITECTURE)
                .toList();
        if (architectureFindings.isEmpty()) {
            md.append("_No architecture drift detected in this PR._\n\n");
        } else {
            appendFindingsTable(md, architectureFindings, null);
        }

        md.append("## QA Findings\n\n");
        List<PrReviewFinding> qaFindings = result.getFindings().stream()
                .filter(f -> f.getArea() == PrReviewArea.QA)
                .toList();
        if (qaFindings.isEmpty()) {
            md.append("_No QA findings detected._\n\n");
        } else {
            appendFindingsTable(md, qaFindings, null);
        }

        md.append("## Recommendation\n\n");
        md.append("### ").append(formatVerdict(result.getVerdict())).append("\n\n");
        md.append(result.getRecommendation()).append("\n\n");

        if (result.getPullRequestSummary() != null && !result.getPullRequestSummary().isBlank()) {
            md.append("## Pull Request Context\n\n");
            md.append(result.getPullRequestSummary()).append("\n\n");
        }

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        md.append("## Review Guidelines\n\n");
        md.append("1. **REJECTED** - blocking QA or architecture gaps must be fixed before merge.\n");
        md.append("2. **APPROVED WITH RISK** - merge allowed with documented follow-up items.\n");
        md.append("3. **APPROVED** - PR meets pre-test quality gates.\n\n");

        return md.toString();
    }

    private static void appendFindingsTable(StringBuilder md,
                                            List<PrReviewFinding> findings,
                                            String emptyMessage) {
        if (findings.isEmpty()) {
            if (emptyMessage != null) {
                md.append(emptyMessage).append("\n\n");
            }
            return;
        }
        md.append("| Severity | Category | Issue | Location | Recommendation |\n");
        md.append("|----------|----------|-------|----------|----------------|\n");
        for (PrReviewFinding finding : findings) {
            md.append("| ").append(finding.getSeverity()).append(" | ")
                    .append(formatCategory(finding.getCategory())).append(" | ")
                    .append(escapePipe(finding.getTitle())).append(" | ")
                    .append(escapePipe(finding.getLocation())).append(" | ")
                    .append(escapePipe(finding.getRecommendation())).append(" |\n");
        }
        md.append("\n");
    }

    private static String formatVerdict(PrReviewVerdict verdict) {
        return switch (verdict) {
            case APPROVED -> "APPROVED";
            case APPROVED_WITH_RISK -> "APPROVED WITH RISK";
            case REJECTED -> "REJECTED";
        };
    }

    private static String formatCategory(PrReviewCategory category) {
        return category.name().replace('_', ' ');
    }

    private static String escapePipe(String value) {
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
