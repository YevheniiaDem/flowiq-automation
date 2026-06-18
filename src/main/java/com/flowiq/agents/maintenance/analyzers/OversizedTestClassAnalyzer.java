package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.List;

public class OversizedTestClassAnalyzer implements MaintenanceAnalyzer {

    private final TestMaintenanceAgentConfig config;

    public OversizedTestClassAnalyzer() {
        this(ConfigFactory.create(TestMaintenanceAgentConfig.class));
    }

    public OversizedTestClassAnalyzer(TestMaintenanceAgentConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "OversizedTestClassAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        int threshold = config.oversizedTestClassLines();

        for (ScannedTestClass test : context.getTestClasses()) {
            if (test.getLineCount() > threshold) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.COMPLEXITY)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Large Test Class")
                        .location(test.getFilePath() + " (" + test.getLineCount() + " lines)")
                        .recommendation("Split " + test.getClassName()
                                + " into focused test classes by scenario or endpoint group.")
                        .priorityRank(3)
                        .build());
            }
        }
        return findings;
    }
}
