package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DuplicateTestAnalyzer implements MaintenanceAnalyzer {

    @Override
    public String name() {
        return "DuplicateTestAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        findings.addAll(findDuplicateEndpointCoverage(context.getTestClasses()));
        findings.addAll(findDuplicateAssertions(context.getTestClasses()));
        return findings;
    }

    private List<MaintenanceFinding> findDuplicateEndpointCoverage(List<ScannedTestClass> tests) {
        Map<String, Set<String>> endpointToClasses = new HashMap<>();
        for (ScannedTestClass test : tests) {
            for (String endpoint : test.getEndpointKeys()) {
                endpointToClasses.computeIfAbsent(endpoint, ignored -> new HashSet<>()).add(test.getClassName());
            }
        }
        List<MaintenanceFinding> findings = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : endpointToClasses.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            findings.add(MaintenanceFinding.builder()
                    .type(MaintenanceFindingType.DUPLICATE)
                    .severity(MaintenanceSeverity.MEDIUM)
                    .title("Duplicate test coverage for endpoint")
                    .location(entry.getKey())
                    .recommendation("Consolidate overlapping tests in: " + String.join(", ", entry.getValue()))
                    .priorityRank(3)
                    .build());
        }
        return findings;
    }

    private List<MaintenanceFinding> findDuplicateAssertions(List<ScannedTestClass> tests) {
        Map<String, Set<String>> assertionToClasses = new HashMap<>();
        for (ScannedTestClass test : tests) {
            for (String assertion : test.getAssertions()) {
                assertionToClasses.computeIfAbsent(assertion, ignored -> new HashSet<>())
                        .add(test.getClassName());
            }
        }
        List<MaintenanceFinding> findings = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : assertionToClasses.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }
            findings.add(MaintenanceFinding.builder()
                    .type(MaintenanceFindingType.DUPLICATE)
                    .severity(MaintenanceSeverity.LOW)
                    .title("Duplicate assertion across test classes")
                    .location(truncate(entry.getKey(), 120))
                    .recommendation("Extract shared assertion helper used by: "
                            + String.join(", ", entry.getValue()))
                    .priorityRank(4)
                    .build());
        }
        return findings;
    }

    private static String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }
}
