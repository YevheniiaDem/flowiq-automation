package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.gap.scanner.EndpointMatcher;
import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedTestClass;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeadTestAnalyzer implements MaintenanceAnalyzer {

    @Override
    public String name() {
        return "DeadTestAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        if (context.getOpenApiEndpoints().isEmpty()) {
            return List.of();
        }
        List<MaintenanceFinding> findings = new ArrayList<>();
        Set<String> openApi = context.getOpenApiEndpoints();

        for (ScannedTestClass test : context.getTestClasses()) {
            for (String endpointKey : test.getEndpointKeys()) {
                if (endpointKey.startsWith("* ")) {
                    String path = endpointKey.substring(2);
                    if (!matchesAnyOpenApi("*", path, openApi)) {
                        findings.add(deadTestFinding(test, endpointKey));
                    }
                    continue;
                }
                int space = endpointKey.indexOf(' ');
                if (space <= 0) {
                    continue;
                }
                String method = endpointKey.substring(0, space);
                String path = endpointKey.substring(space + 1);
                if (!matchesAnyOpenApi(method, path, openApi)) {
                    findings.add(deadTestFinding(test, endpointKey));
                }
            }
        }
        return findings;
    }

    private static boolean matchesAnyOpenApi(String method, String path, Set<String> openApi) {
        for (String endpoint : openApi) {
            int space = endpoint.indexOf(' ');
            if (space <= 0) {
                continue;
            }
            String apiMethod = endpoint.substring(0, space);
            String apiPath = endpoint.substring(space + 1);
            if (EndpointMatcher.matches(path, apiPath)
                    && (method.equals("*") || EndpointMatcher.methodMatches(method, apiMethod))) {
                return true;
            }
        }
        return false;
    }

    private static MaintenanceFinding deadTestFinding(ScannedTestClass test, String endpointKey) {
        return MaintenanceFinding.builder()
                .type(MaintenanceFindingType.DEAD_CODE)
                .severity(MaintenanceSeverity.HIGH)
                .title("Endpoint removed but tests still exist")
                .location(test.getClassName() + " -> " + endpointKey)
                .recommendation("Remove or update obsolete test " + test.getClassName()
                        + " for removed endpoint " + endpointKey + ".")
                .priorityRank(1)
                .build();
    }
}
