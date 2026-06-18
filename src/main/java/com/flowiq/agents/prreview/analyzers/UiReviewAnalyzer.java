package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.prreview.model.PrChangedArtifact;
import com.flowiq.agents.prreview.model.PrChangedArtifactType;
import com.flowiq.agents.prreview.model.PrReviewArea;
import com.flowiq.agents.prreview.model.PrReviewCategory;
import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.scanner.PrReviewContext;
import com.flowiq.agents.prreview.scanner.SourceInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UiReviewAnalyzer implements PrReviewAnalyzer {

    private static final Pattern XPATH = Pattern.compile(
            "(?i)(page\\.locator\\(\\s*\"(xpath=|//)|\\.locator\\(\\s*\"//|By\\.xpath|getByXPath)");
    private static final Pattern UNSTABLE_CSS = Pattern.compile(
            "(?i)(page\\.locator\\(\\s*\"[^\"]*\\.(css|scss)|locator\\(\\s*\"[^\"]*\\[[^=]+='[^']+'\\]|"
                    + "locator\\(\\s*\"[^\"]*nth-child|locator\\(\\s*\"section \\.|locator\\(\\s*\"div \\.)");
    private static final Pattern DATA_TESTID = Pattern.compile(
            "(?i)(byTestId\\(|getByTestId\\(|data-testid|TestIds\\.)");

    @Override
    public String name() {
        return "UiReviewAnalyzer";
    }

    @Override
    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        SourceInventory inventory = context.getSourceInventory();

        for (PrChangedArtifact artifact : context.getChangedArtifacts()) {
            if (artifact.getType() == PrChangedArtifactType.PAGE) {
                findings.addAll(reviewPageSource(artifact));
            }
        }

        for (String pageFile : inventory.getPageObjectFiles()) {
            if (context.getChangedFiles().stream().noneMatch(f -> normalize(f).equals(normalize(pageFile)))) {
                continue;
            }
            String content = readContent(context, pageFile);
            if (content.isBlank()) {
                continue;
            }
            findings.addAll(reviewLocatorPatterns(pageFile, content));
        }
        return findings;
    }

    private List<PrReviewFinding> reviewPageSource(PrChangedArtifact page) {
        String content = page.getSourceContent();
        if (content == null || content.isBlank()) {
            return List.of();
        }
        String location = page.getFilePath() != null ? page.getFilePath() : page.getName();
        return reviewLocatorPatterns(location, content);
    }

    private List<PrReviewFinding> reviewLocatorPatterns(String location, String content) {
        List<PrReviewFinding> findings = new ArrayList<>();

        if (XPATH.matcher(content).find()) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.UI_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.HIGH)
                    .title("XPath locator usage detected")
                    .location(location)
                    .recommendation("Replace XPath selectors with data-testid or role-based Playwright locators.")
                    .blocking(false)
                    .build());
        }
        if (UNSTABLE_CSS.matcher(content).find()) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.UI_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Potentially unstable CSS selector")
                    .location(location)
                    .recommendation("Prefer TestIds/byTestId() over structural CSS selectors for maintainability.")
                    .blocking(false)
                    .build());
        }
        if (!DATA_TESTID.matcher(content).find() && content.contains("locator(")) {
            findings.add(PrReviewFinding.builder()
                    .category(PrReviewCategory.UI_REVIEW)
                    .area(PrReviewArea.ARCHITECTURE)
                    .severity(PrReviewSeverity.MEDIUM)
                    .title("Missing data-testid based locators")
                    .location(location)
                    .recommendation("Add data-testid attributes and reference them via TestIds constants.")
                    .blocking(false)
                    .build());
        }
        return findings;
    }

    private static String readContent(PrReviewContext context, String pageFile) {
        return context.getChangedArtifacts().stream()
                .filter(a -> pageFile.equals(a.getFilePath()))
                .map(PrChangedArtifact::getSourceContent)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("");
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }
}
