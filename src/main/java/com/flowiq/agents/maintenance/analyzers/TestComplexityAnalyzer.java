package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import com.flowiq.agents.maintenance.config.TestMaintenanceAgentConfig;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedPageObject;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestComplexityAnalyzer implements MaintenanceAnalyzer {

    private final TestMaintenanceAgentConfig config;

    public TestComplexityAnalyzer() {
        this(ConfigFactory.create(TestMaintenanceAgentConfig.class));
    }

    public TestComplexityAnalyzer(TestMaintenanceAgentConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "TestComplexityAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        findings.addAll(findLongMethods(context.getTestClasses()));
        findings.addAll(findLowCohesionPages(context.getPageObjects()));
        findings.addAll(findFlakyCandidates(context.getAllureRecords()));
        return findings;
    }

    private List<MaintenanceFinding> findLongMethods(List<ScannedTestClass> tests) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        int threshold = config.longMethodLines();
        for (ScannedTestClass test : tests) {
            if (test.getMaxMethodLines() > threshold) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.COMPLEXITY)
                        .severity(MaintenanceSeverity.HIGH)
                        .title("Long test method detected")
                        .location(test.getClassName() + " (max " + test.getMaxMethodLines() + " lines)")
                        .recommendation("Extract setup/assertion helpers to reduce method length in "
                                + test.getClassName() + ".")
                        .priorityRank(2)
                        .build());
            }
        }
        return findings;
    }

    private List<MaintenanceFinding> findLowCohesionPages(List<ScannedPageObject> pages) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        int threshold = config.lowCohesionMethodCount();
        for (ScannedPageObject page : pages) {
            if (page.getPublicMethodCount() > threshold) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.COMPLEXITY)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("Low cohesion Page Object")
                        .location(page.getClassName() + " (" + page.getPublicMethodCount() + " public methods)")
                        .recommendation("Split " + page.getClassName()
                                + " into section components to improve cohesion.")
                        .priorityRank(3)
                        .build());
            }
        }
        return findings;
    }

    private List<MaintenanceFinding> findFlakyCandidates(List<TestExecutionRecord> records) {
        Map<String, int[]> stats = new HashMap<>();
        for (TestExecutionRecord record : records) {
            int[] counter = stats.computeIfAbsent(record.getTestKey(), ignored -> new int[2]);
            counter[1]++;
            if (record.getOutcome() == TestOutcome.FAILED || record.getOutcome() == TestOutcome.BROKEN) {
                counter[0]++;
            }
        }

        List<MaintenanceFinding> findings = new ArrayList<>();
        double threshold = config.flakyFailureThreshold();
        for (Map.Entry<String, int[]> entry : stats.entrySet()) {
            int failures = entry.getValue()[0];
            int total = entry.getValue()[1];
            if (total < 2) {
                continue;
            }
            double rate = (double) failures / total;
            if (rate >= threshold && failures > 0) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.FLAKY)
                        .severity(rate >= 0.5 ? MaintenanceSeverity.CRITICAL : MaintenanceSeverity.HIGH)
                        .title("Flaky test candidate")
                        .location(entry.getKey())
                        .recommendation(String.format(
                                "Investigate instability (%d/%d failures, %.0f%%). Review traces and stabilize waits.",
                                failures, total, rate * 100))
                        .priorityRank(1)
                        .build());
            }
        }
        return findings;
    }
}
