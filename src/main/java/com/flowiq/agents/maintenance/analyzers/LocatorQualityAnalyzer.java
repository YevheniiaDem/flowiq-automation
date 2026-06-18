package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LocatorQualityAnalyzer implements MaintenanceAnalyzer {

    private static final Pattern XPATH = Pattern.compile(
            "(?i)(page\\.locator\\(\\s*\"(xpath=|//)|\\.locator\\(\\s*\"//|By\\.xpath|getByXPath)");
    private static final Pattern NTH_CHILD = Pattern.compile(
            "(?i)(nth-child|nth-of-type|:nth\\()");

    @Override
    public String name() {
        return "LocatorQualityAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        for (ScannedPageObject page : context.getPageObjects()) {
            findings.addAll(analyzeSource(page.getFilePath(), page.getSource()));
        }
        for (ScannedTestClass test : context.getTestClasses()) {
            if (test.getFilePath().contains("/ui/")) {
                findings.addAll(analyzeSource(test.getFilePath(), test.getSource()));
            }
        }
        return findings;
    }

    private List<MaintenanceFinding> analyzeSource(String location, String source) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        if (source == null || source.isBlank()) {
            return findings;
        }
        if (XPATH.matcher(source).find()) {
            findings.add(MaintenanceFinding.builder()
                    .type(MaintenanceFindingType.LOCATOR_QUALITY)
                    .severity(MaintenanceSeverity.HIGH)
                    .title("XPath usage detected")
                    .location(location)
                    .recommendation("Replace XPath with data-testid or role-based Playwright locators.")
                    .priorityRank(2)
                    .build());
        }
        if (NTH_CHILD.matcher(source).find()) {
            findings.add(MaintenanceFinding.builder()
                    .type(MaintenanceFindingType.LOCATOR_QUALITY)
                    .severity(MaintenanceSeverity.MEDIUM)
                    .title("nth-child selector usage detected")
                    .location(location)
                    .recommendation("Avoid nth-child selectors; use stable test ids or semantic roles.")
                    .priorityRank(3)
                    .build());
        }
        return findings;
    }
}
