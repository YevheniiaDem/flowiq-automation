package com.flowiq.ci.flaky.report;

import com.flowiq.ci.flaky.model.CiFlakyReport;
import com.flowiq.ci.flaky.model.CiFlakyTestEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CiFlakySummaryWriter {

    public List<String> buildSummaryLines(CiFlakyReport report) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("Current run: **%d passed**, **%d failed** (%d tests).",
                report.getCurrentRunPassed(), report.getCurrentRunFailed(), report.getCurrentRunTotal()));
        lines.add(String.format("**%d flaky** (historical instability) · **%d failed-only** (not flaky).",
                report.getFlakyCount(), report.getFailedOnlyCount()));
        lines.add(String.format("Duration-unstable: %d · Recovered this run: %d · History runs: %d.",
                report.getDurationUnstableCount(), report.getRecoveredThisRunCount(), report.getHistoryRunCount()));
        return lines;
    }

    public void writeGitHubStepSummary(CiFlakyReport report, Path summaryFile) throws IOException {
        if (summaryFile == null || summaryFile.toString().equals("/dev/null")) {
            return;
        }

        StringBuilder md = new StringBuilder();
        md.append("## Flaky Test Detection\n\n");
        for (String line : buildSummaryLines(report)) {
            md.append("- ").append(line).append("\n");
        }

        md.append("\n### Failed Tests (current run — not classified as flaky)\n\n");
        if (report.getFailedTests().isEmpty()) {
            md.append("_None._\n\n");
        } else {
            md.append("| Test | Suite | Outcome |\n|------|-------|--------|\n");
            for (CiFlakyTestEntry entry : report.getFailedTests()) {
                md.append("| `").append(entry.getMethodName()).append("` | ")
                        .append(entry.getSuite()).append(" | ")
                        .append(entry.getCurrentOutcome()).append(" |\n");
            }
            md.append("\n");
        }

        md.append("### Flaky Tests (historical instability)\n\n");
        md.append("_Intermittent pass/fail or unstable duration. CI infrastructure retries are **not** included._\n\n");
        if (report.getFlakyTests().isEmpty()) {
            md.append("_None detected across history._\n\n");
        } else {
            md.append("| Test | Type | Runs | Pass/Fail | Flaky % | CV | Current | Recovered |\n");
            md.append("|------|------|------|-----------|---------|----|---------|-----------|\n");
            for (CiFlakyTestEntry entry : report.getFlakyTests()) {
                md.append("| `").append(entry.getMethodName()).append("` | ")
                        .append(entry.getClassification()).append(" | ")
                        .append(entry.getTotalRuns()).append(" | ")
                        .append(entry.getPassCount()).append("/").append(entry.getFailCount()).append(" | ")
                        .append(String.format("%.0f", entry.getFlakinessPercent())).append("% | ")
                        .append(String.format("%.2f", entry.getDurationCv())).append(" | ")
                        .append(entry.getCurrentOutcome()).append(" | ")
                        .append(entry.isRecoveredThisRun() ? "yes" : "no").append(" |\n");
            }
            md.append("\n");
        }

        md.append("Reports: `flaky-report.json`, `flaky-report.html` (workflow artifacts).\n");

        Path parent = summaryFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(summaryFile, md.toString(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND);
    }
}
