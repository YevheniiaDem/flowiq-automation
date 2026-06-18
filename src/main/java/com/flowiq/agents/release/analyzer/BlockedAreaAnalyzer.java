package com.flowiq.agents.release.analyzer;

import com.flowiq.agents.release.model.BlockedArea;
import com.flowiq.agents.release.model.CriticalFailure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockedAreaAnalyzer {

    public List<BlockedArea> analyze(List<CriticalFailure> failures) {
        Map<String, List<CriticalFailure>> byModule = failures.stream()
                .collect(Collectors.groupingBy(CriticalFailure::getModule, LinkedHashMap::new, Collectors.toList()));

        List<BlockedArea> areas = new ArrayList<>();
        for (Map.Entry<String, List<CriticalFailure>> entry : byModule.entrySet()) {
            List<CriticalFailure> moduleFailures = entry.getValue();
            List<String> suites = moduleFailures.stream()
                    .map(f -> f.getSuiteType().name().toLowerCase())
                    .distinct()
                    .toList();
            List<String> tests = moduleFailures.stream()
                    .map(CriticalFailure::getTestKey)
                    .distinct()
                    .limit(5)
                    .toList();

            areas.add(BlockedArea.builder()
                    .module(entry.getKey())
                    .reason(buildReason(moduleFailures))
                    .failureCount(moduleFailures.size())
                    .affectedSuites(suites)
                    .affectedTests(tests)
                    .build());
        }
        areas.sort(Comparator.comparing(BlockedArea::getFailureCount).reversed());
        return areas;
    }

    private static String buildReason(List<CriticalFailure> failures) {
        boolean smoke = failures.stream().anyMatch(f -> f.getSuiteType().name().equals("SMOKE"));
        boolean contract = failures.stream().anyMatch(f -> f.getSuiteType().name().equals("CONTRACT"));
        if (smoke) {
            return "Smoke suite failure blocks release confidence for this module";
        }
        if (contract) {
            return "Contract validation failure indicates API drift";
        }
        return "Regression failure in core business flows";
    }
}
