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

public class ServiceTestDriftChecker implements ArchitectureDriftChecker {

    @Override
    public List<ArchitectureDriftIssue> check(ArchitectureContext context) {
        List<ArchitectureDriftIssue> issues = new ArrayList<>();
        Set<String> regressionTests = context.getRegressionTestClasses();
        Set<String> smokeTests = context.getSmokeTestClasses();

        for (SourceArtifact service : context.getServices()) {
            String module = ModuleAliases.normalize(service.getModule());
            if (!hasTestCoverage(module, regressionTests, smokeTests)) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.SERVICE_WITHOUT_TESTS)
                        .issue("Backend service client has no mapped regression or smoke tests")
                        .severity(DriftSeverity.HIGH)
                        .location(service.getRelativePath())
                        .recommendation("Add API regression or smoke tests for module '" + module + "'.")
                        .build());
            }
        }
        return issues;
    }

    private boolean hasTestCoverage(String module, Set<String> regressionTests, Set<String> smokeTests) {
        String token = ModuleAliases.moduleToken(module);
        return regressionTests.stream().anyMatch(name -> matchesModule(name, token))
                || smokeTests.stream().anyMatch(name -> matchesModule(name, token));
    }

    private static boolean matchesModule(String testClass, String moduleToken) {
        return testClass.toLowerCase(Locale.ROOT).replace("-", "").contains(moduleToken);
    }
}
