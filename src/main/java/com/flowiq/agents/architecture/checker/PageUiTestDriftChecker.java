package com.flowiq.agents.architecture.checker;

import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.inventory.SourceArtifact;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.DriftIssueType;
import com.flowiq.agents.architecture.model.DriftSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PageUiTestDriftChecker implements ArchitectureDriftChecker {

    @Override
    public List<ArchitectureDriftIssue> check(ArchitectureContext context) {
        List<ArchitectureDriftIssue> issues = new ArrayList<>();
        Set<String> uiTests = context.getUiTestClasses();

        for (SourceArtifact page : context.getPages()) {
            if ("LoginPage".equals(page.getName())) {
                continue;
            }
            if (!hasUiTest(page, uiTests)) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.PAGE_WITHOUT_UI_TESTS)
                        .issue("Page object has no UI smoke test coverage")
                        .severity(DriftSeverity.MEDIUM)
                        .location(page.getRelativePath())
                        .recommendation("Add UI smoke test in com.flowiq.ui.smoke for page '"
                                + page.getName() + "'.")
                        .build());
            }
        }
        return issues;
    }

    private static boolean hasUiTest(SourceArtifact page, Set<String> uiTests) {
        String pageToken = page.getName().replace("Page", "").toLowerCase(Locale.ROOT);
        String moduleToken = ModuleAliases.moduleToken(page.getModule());
        return uiTests.stream().anyMatch(test -> {
            String lower = test.toLowerCase(Locale.ROOT);
            return lower.contains(pageToken) || lower.contains(moduleToken);
        });
    }
}
