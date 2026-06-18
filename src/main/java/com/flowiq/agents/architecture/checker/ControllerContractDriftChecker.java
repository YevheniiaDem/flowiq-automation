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

public class ControllerContractDriftChecker implements ArchitectureDriftChecker {

    @Override
    public List<ArchitectureDriftIssue> check(ArchitectureContext context) {
        List<ArchitectureDriftIssue> issues = new ArrayList<>();
        Set<String> contractTests = context.getContractTestClasses();

        for (SourceArtifact controller : context.getControllers()) {
            if (!hasContractTest(controller.getModule(), contractTests)) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.CONTROLLER_WITHOUT_CONTRACT_TESTS)
                        .issue("Controller has no contract test coverage")
                        .severity(DriftSeverity.CRITICAL)
                        .location(controller.getRelativePath())
                        .recommendation("Add contract tests and JSON schemas for controller module '"
                                + ModuleAliases.normalize(controller.getModule()) + "'.")
                        .build());
            }
        }

        for (SourceArtifact service : context.getServices()) {
            String module = ModuleAliases.normalize(service.getModule());
            if (expectsContract(module) && !hasContractTest(module, contractTests)) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.CONTROLLER_WITHOUT_CONTRACT_TESTS)
                        .issue("API module exposed via service client lacks contract tests")
                        .severity(DriftSeverity.HIGH)
                        .location(service.getRelativePath())
                        .recommendation("Add " + capitalize(module) + "ContractTest with JSON schema validation.")
                        .build());
            }
        }
        return issues;
    }

    private static boolean expectsContract(String module) {
        return !Set.of("imports", "dashboard").contains(module);
    }

    private static boolean hasContractTest(String module, Set<String> contractTests) {
        String token = ModuleAliases.moduleToken(module);
        return contractTests.stream().anyMatch(name -> name.toLowerCase(Locale.ROOT)
                .replace("-", "").contains(token));
    }

    private static String capitalize(String module) {
        if (module == null || module.isBlank()) {
            return "Module";
        }
        String[] parts = module.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
