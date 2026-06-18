package com.flowiq.agents.generator.analysis;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioType;
import com.flowiq.agents.generator.schema.JsonSchemaDocument;
import com.flowiq.agents.generator.schema.JsonSchemaIndex;
import com.flowiq.agents.gap.matrix.CoverageMatrixBuilder;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class EndpointCoverageAnalyzer {

    private final CoverageMatrixBuilder coverageMatrixBuilder = new CoverageMatrixBuilder();

    public List<EndpointTestContext> analyze(JsonNode openApiSpec,
                                             List<com.flowiq.agents.gap.scanner.ScannedTestReference> tests,
                                             JsonSchemaIndex schemaIndex) {
        List<EndpointCoverage> coverages = coverageMatrixBuilder.build(openApiSpec, tests);
        return OpenApiNavigator.getOperations(openApiSpec).stream()
                .map(op -> toContext(op, coverages, schemaIndex))
                .toList();
    }

    private EndpointTestContext toContext(OpenApiOperation operation,
                                          List<EndpointCoverage> coverages,
                                          JsonSchemaIndex schemaIndex) {
        String path = TestSourceScanner.normalizePath(operation.path());
        EndpointCoverage coverage = coverages.stream()
                .filter(c -> c.getPath().equals(path) && c.getMethod().equals(operation.method()))
                .findFirst()
                .orElse(null);

        boolean requiresAuth = coverage != null && coverage.isRequiresAuth();
        Set<ScenarioType> covered = EnumSet.noneOf(ScenarioType.class);
        if (coverage != null) {
            if (coverage.isRegressionCovered() || coverage.isSmokeCovered()) {
                covered.add(ScenarioType.POSITIVE);
            }
            if (coverage.isNegativeCovered()) {
                covered.add(ScenarioType.NEGATIVE);
            }
            if (coverage.isAuthorizationCovered()) {
                covered.add(ScenarioType.AUTHORIZATION);
            }
            if (coverage.isContractCovered()) {
                covered.add(ScenarioType.BOUNDARY);
            }
        }

        JsonSchemaDocument schema = schemaIndex.findForEndpoint(path, operation.method()).orElse(null);

        return EndpointTestContext.builder()
                .operation(operation)
                .normalizedPath(path)
                .module(coverage != null ? coverage.getModule() : extractModule(path))
                .requiresAuth(requiresAuth)
                .coverage(coverage)
                .responseSchema(schema)
                .coveredScenarioTypes(covered)
                .build();
    }

    private static String extractModule(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        return slash >= 0 ? normalized.substring(0, slash) : normalized;
    }
}
