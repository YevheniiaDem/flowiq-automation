package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.List;

public class OversizedPageObjectAnalyzer implements MaintenanceAnalyzer {

    private final TestMaintenanceAgentConfig config;

    public OversizedPageObjectAnalyzer() {
        this(ConfigFactory.create(TestMaintenanceAgentConfig.class));
    }

    public OversizedPageObjectAnalyzer(TestMaintenanceAgentConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "OversizedPageObjectAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        int threshold = config.oversizedPageObjectLines();

        for (ScannedPageObject page : context.getPageObjects()) {
            if (page.getLineCount() > threshold) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.COMPLEXITY)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Large Page Object")
                        .location(page.getFilePath() + " (" + page.getLineCount() + " lines)")
                        .recommendation("Split " + page.getClassName()
                                + " into smaller components or section objects.")
                        .priorityRank(3)
                        .build());
            }
        }
        return findings;
    }
}
