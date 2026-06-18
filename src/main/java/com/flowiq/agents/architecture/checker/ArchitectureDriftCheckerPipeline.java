package com.flowiq.agents.architecture.checker;

import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArchitectureDriftCheckerPipeline {

    private final List<ArchitectureDriftChecker> checkers = List.of(
            new EndpointDocumentationDriftChecker(),
            new ServiceTestDriftChecker(),
            new ControllerContractDriftChecker(),
            new PageUiTestDriftChecker(),
            new DtoSchemaDriftChecker()
    );

    public List<ArchitectureDriftIssue> analyze(ArchitectureContext context) {
        List<ArchitectureDriftIssue> issues = new ArrayList<>();
        for (ArchitectureDriftChecker checker : checkers) {
            issues.addAll(checker.check(context));
        }
        issues.sort(Comparator.comparing((ArchitectureDriftIssue issue) -> issue.getSeverity().ordinal())
                .thenComparing(ArchitectureDriftIssue::getType));
        return issues;
    }
}
