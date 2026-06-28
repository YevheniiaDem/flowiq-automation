package com.flowiq.ci.flaky.report;

import com.flowiq.ci.flaky.model.CiFlakyReport;
import com.flowiq.ci.flaky.model.CiFlakyTestEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class CiFlakyHtmlReportWriter {

    public void write(CiFlakyReport report, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        html.append("<title>FlowIQ Flaky Test Report</title>");
        html.append("<style>");
        html.append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#1f2937;}");
        html.append("h1,h2{color:#111827;} table{border-collapse:collapse;width:100%;margin:16px 0;}");
        html.append("th,td{border:1px solid #e5e7eb;padding:8px 10px;text-align:left;font-size:14px;}");
        html.append("th{background:#f3f4f6;} .failed{background:#fef2f2;} .flaky{background:#fffbeb;}");
        html.append(".badge{display:inline-block;padding:2px 8px;border-radius:999px;font-size:12px;}");
        html.append(".badge-failed{background:#fecaca;} .badge-flaky{background:#fde68a;}");
        html.append(".meta{color:#6b7280;font-size:14px;}");
        html.append("</style></head><body>");

        html.append("<h1>FlowIQ Flaky Test Report</h1>");
        html.append("<p class=\"meta\">Run <strong>").append(escape(report.getRunId())).append("</strong> · ");
        html.append("Workflow <strong>").append(escape(report.getWorkflow())).append("</strong> · ");
        html.append("Analyzed at ").append(report.getAnalyzedAt()).append("</p>");

        html.append("<h2>Summary</h2><ul>");
        html.append("<li>Current run: ").append(report.getCurrentRunPassed()).append(" passed, ");
        html.append(report.getCurrentRunFailed()).append(" failed (").append(report.getCurrentRunTotal()).append(" total)</li>");
        html.append("<li><span class=\"badge badge-failed\">Failed only</span> ").append(report.getFailedOnlyCount());
        html.append(" — hard failures not classified as flaky</li>");
        html.append("<li><span class=\"badge badge-flaky\">Flaky</span> ").append(report.getFlakyCount());
        html.append(" — intermittent or duration-unstable across ").append(report.getHistoryRunCount());
        html.append(" historical runs</li>");
        html.append("<li>Duration-unstable: ").append(report.getDurationUnstableCount()).append("</li>");
        html.append("<li>Recovered this run (passed after historical failures): ")
                .append(report.getRecoveredThisRunCount()).append("</li>");
        html.append("</ul>");

        html.append("<h2>Failed Tests (current run, not flaky)</h2>");
        html.append(renderTable(report.getFailedTests(), false));

        html.append("<h2>Flaky Tests (historical instability)</h2>");
        html.append("<p class=\"meta\">Includes intermittent pass/fail patterns and unstable duration. ");
        html.append("CI infrastructure retries are excluded.</p>");
        html.append(renderTable(report.getFlakyTests(), true));

        html.append("</body></html>");
        Files.writeString(outputFile, html.toString(), StandardCharsets.UTF_8);
    }

    private String renderTable(java.util.List<CiFlakyTestEntry> entries, boolean flakySection) {
        if (entries.isEmpty()) {
            return "<p><em>None detected.</em></p>";
        }
        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr>");
        table.append("<th>Test</th><th>Suite</th><th>Classification</th>");
        table.append("<th>Runs</th><th>Pass</th><th>Fail</th><th>Flaky %</th>");
        table.append("<th>Duration CV</th><th>Avg ms</th><th>Current</th>");
        if (flakySection) {
            table.append("<th>Recovered</th>");
        }
        table.append("</tr></thead><tbody>");

        for (CiFlakyTestEntry entry : entries) {
            String rowClass = flakySection ? "flaky" : "failed";
            table.append("<tr class=\"").append(rowClass).append("\">");
            table.append("<td>").append(escape(entry.getClassName())).append(".")
                    .append(escape(entry.getMethodName())).append("</td>");
            table.append("<td>").append(escape(entry.getSuite())).append("</td>");
            table.append("<td>").append(entry.getClassification() != null
                    ? escape(entry.getClassification().name()) : "FAILED").append("</td>");
            table.append("<td>").append(entry.getTotalRuns()).append("</td>");
            table.append("<td>").append(entry.getPassCount()).append("</td>");
            table.append("<td>").append(entry.getFailCount()).append("</td>");
            table.append("<td>").append(String.format("%.1f", entry.getFlakinessPercent())).append("</td>");
            table.append("<td>").append(String.format("%.2f", entry.getDurationCv())).append("</td>");
            table.append("<td>").append(entry.getAvgDurationMs()).append("</td>");
            table.append("<td>").append(escape(entry.getCurrentOutcome())).append("</td>");
            if (flakySection) {
                table.append("<td>").append(entry.isRecoveredThisRun() ? "yes" : "no").append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</tbody></table>");
        return table.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.chars()
                .mapToObj(c -> switch (c) {
                    case '<' -> "&lt;";
                    case '>' -> "&gt;";
                    case '&' -> "&amp;";
                    default -> String.valueOf((char) c);
                })
                .collect(Collectors.joining());
    }
}
