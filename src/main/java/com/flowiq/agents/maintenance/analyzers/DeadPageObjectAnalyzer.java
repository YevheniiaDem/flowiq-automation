package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;

public class DeadPageObjectAnalyzer implements MaintenanceAnalyzer {

    @Override
    public String name() {
        return "DeadPageObjectAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        String sources = context.getCombinedMainAndTestSources();
        List<MaintenanceFinding> findings = new ArrayList<>();

        for (ScannedPageObject page : context.getPageObjects()) {
            if (!isReferenced(page.getClassName(), sources)) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DEAD_CODE)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Page Object not referenced")
                        .location(page.getFilePath())
                        .recommendation("Remove unused page object " + page.getClassName()
                                + " or add UI tests that reference it.")
                        .priorityRank(2)
                        .build());
            }
        }
        return findings;
    }

    private static boolean isReferenced(String className, String sources) {
        return sources.contains(className)
                || sources.contains("new " + className)
                || sources.contains(className + ".class");
    }
}
