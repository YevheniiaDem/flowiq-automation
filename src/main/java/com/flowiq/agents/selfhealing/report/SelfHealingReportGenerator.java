package com.flowiq.agents.selfhealing.report;

import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import com.flowiq.agents.selfhealing.model.SelfHealingResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SelfHealingReportGenerator {

    private final SelfHealingAgentConfig config;

    public SelfHealingReportGenerator(SelfHealingAgentConfig config) {
        this.config = config;
    }

    public Path generate(SelfHealingResult result) {
        Path outputPath = resolveReportPath();
        try {
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, buildMarkdown(result));
            log.info("Self-healing report written to {}", outputPath.toAbsolutePath());
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write report to " + outputPath, e);
        }
    }

    private String buildMarkdown(SelfHealingResult result) {
        StringBuilder md = new StringBuilder();
        md.append("# Self-Healing Locator Report\n\n");
        md.append("_Automated Playwright locator recovery suggestions_\n\n");
        md.append("Generated: ")
                .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(result.getAnalyzedAt().atOffset(ZoneOffset.UTC)))
                .append("\n\n");

        md.append("## Executive Summary\n\n");
        if (result.getExecutiveSummary().isEmpty()) {
            md.append("- Analysis completed.\n");
        } else {
            result.getExecutiveSummary().forEach(line -> md.append("- ").append(line).append("\n"));
        }
        md.append("\n");

        md.append("| Metric | Value |\n");
        md.append("|--------|-------|\n");
        md.append("| Failures analyzed | ").append(result.getFailuresAnalyzed()).append(" |\n");
        md.append("| Suggestions generated | ").append(result.getSuggestionsGenerated()).append(" |\n");
        md.append("\n");

        if (result.getSuggestions().isEmpty()) {
            md.append("_No locator healing suggestions produced — no UI failures with DOM snapshots found._\n\n");
        } else {
            int index = 1;
            for (LocatorSuggestion suggestion : result.getSuggestions()) {
                md.append("## Suggestion ").append(index++).append("\n\n");
                appendField(md, "Test", suggestion.getTestName() + " (`" + suggestion.getTestKey() + "`)");
                appendField(md, "Old Locator", "`" + suggestion.getOldLocator() + "`");
                appendField(md, "Suggested Locator", "`" + suggestion.getSuggestedLocator() + "`");
                appendField(md, "Confidence", suggestion.getConfidence().name()
                        + " (score " + suggestion.getSimilarityScore() + ")");
                appendField(md, "Reasoning", suggestion.getReasoning());
                appendField(md, "Risk", suggestion.getRisk().name());
                md.append("- **Screenshot:** ").append(suggestion.getScreenshotPath()).append("\n");
                md.append("- **DOM Snapshot:** ").append(suggestion.getDomSnapshotPath()).append("\n");
                if (suggestion.isLlmEnriched()) {
                    md.append("- **LLM enriched:** yes\n");
                }
                md.append("\n---\n\n");
            }
        }

        md.append("## Implementation Notes\n\n");
        md.append("1. Prefer `data-testid` suggestions (LOW risk) over CSS/text selectors.\n");
        md.append("2. Validate suggested locators in headed mode: `mvn test -Pui-headed -Dtest=...`\n");
        md.append("3. Update Page Objects under `com.flowiq.pages` after manual verification.\n");
        md.append("4. Re-run with `-Pself-healing` after the next UI failure batch.\n\n");

        md.append("## Data Sources\n\n");
        md.append(result.getDataSourcesSummary()).append("\n\n");

        return md.toString();
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
