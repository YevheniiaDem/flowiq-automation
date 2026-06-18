package com.flowiq.agents.architecture.checker;

import com.flowiq.agents.architecture.inventory.ApiEndpointRef;
import com.flowiq.agents.architecture.inventory.ArchitectureContext;
import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.DriftIssueType;
import com.flowiq.agents.architecture.model.DriftSeverity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EndpointDocumentationDriftChecker implements ArchitectureDriftChecker {

    @Override
    public List<ArchitectureDriftIssue> check(ArchitectureContext context) {
        Set<String> documentedKeys = new LinkedHashSet<>();
        context.getDocumentedEndpoints().forEach(endpoint -> documentedKeys.add(endpoint.key()));

        Set<String> openApiKeys = new LinkedHashSet<>();
        context.getOpenApiEndpoints().forEach(endpoint -> openApiKeys.add(endpoint.key()));

        List<ArchitectureDriftIssue> issues = new ArrayList<>();

        for (ApiEndpointRef endpoint : context.getOpenApiEndpoints()) {
            if (!documentedKeys.contains(endpoint.key())) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.ENDPOINT_WITHOUT_DOCUMENTATION)
                        .issue("OpenAPI endpoint is not documented in docs/ or ADR")
                        .severity(DriftSeverity.HIGH)
                        .location(endpoint.getMethod() + " " + endpoint.getPath())
                        .recommendation("Add endpoint to CONTRACT-COVERAGE.md or module regression documentation.")
                        .build());
            }
        }

        for (ApiEndpointRef endpoint : context.getDocumentedEndpoints()) {
            if (!openApiKeys.isEmpty() && !openApiKeys.contains(endpoint.key())) {
                issues.add(ArchitectureDriftIssue.builder()
                        .type(DriftIssueType.DOCUMENTATION_WITHOUT_ENDPOINT)
                        .issue("Documented endpoint is missing from OpenAPI specification")
                        .severity(DriftSeverity.MEDIUM)
                        .location(endpoint.getMethod() + " " + endpoint.getPath()
                                + " (" + endpoint.getSource() + ")")
                        .recommendation("Update OpenAPI spec or remove stale documentation.")
                        .build());
            }
        }
        return issues;
    }
}
