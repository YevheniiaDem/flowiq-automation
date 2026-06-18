package com.flowiq.agents.gap.matrix;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.scanner.EndpointMatcher;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.gap.scanner.TestSourceScanner;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.openapi.OpenApiNavigator;
import com.flowiq.agents.openapi.OpenApiOperation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CoverageMatrixBuilder {

    public List<EndpointCoverage> build(JsonNode spec, List<ScannedTestReference> testReferences) {
        List<EndpointCoverage> coverages = new ArrayList<>();
        for (OpenApiOperation operation : OpenApiNavigator.getOperations(spec)) {
            String path = TestSourceScanner.normalizePath(operation.path());
            String module = extractModule(path);
            boolean requiresAuth = requiresAuthentication(operation, path);

            boolean contract = false;
            boolean smoke = false;
            boolean regression = false;
            boolean ui = false;
            boolean negative = false;
            boolean authorization = false;
            Set<String> tests = new LinkedHashSet<>();

            for (ScannedTestReference reference : testReferences) {
                if (!EndpointMatcher.matches(reference.getPath(), path)) {
                    continue;
                }
                if (!EndpointMatcher.methodMatches(reference.getMethod(), operation.method())) {
                    continue;
                }
                tests.add(reference.getClassName());
                if (reference.getSuites().contains(TestSuiteType.CONTRACT)) contract = true;
                if (reference.getSuites().contains(TestSuiteType.SMOKE)) smoke = true;
                if (reference.getSuites().contains(TestSuiteType.REGRESSION)) regression = true;
                if (reference.getSuites().contains(TestSuiteType.UI)) ui = true;
                if (reference.isNegativeScenario()) negative = true;
                if (reference.isAuthorizationCheck()) authorization = true;
            }

            coverages.add(EndpointCoverage.builder()
                    .path(path)
                    .method(operation.method())
                    .module(module)
                    .contractCovered(contract)
                    .smokeCovered(smoke)
                    .regressionCovered(regression)
                    .uiCovered(ui)
                    .negativeCovered(negative)
                    .authorizationCovered(authorization)
                    .requiresAuth(requiresAuth)
                    .coveringTests(tests)
                    .build());
        }
        return coverages;
    }

    private static boolean requiresAuthentication(OpenApiOperation operation, String path) {
        if (path.startsWith("/auth/login") || path.startsWith("/auth/register")
                || path.startsWith("/health")) {
            return false;
        }
        JsonNode op = operation.operation();
        if (op != null && op.has("security") && op.get("security").isArray() && !op.get("security").isEmpty()) {
            return true;
        }
        return !path.startsWith("/auth/");
    }

    static String extractModule(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "default";
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        int slash = normalized.indexOf('/');
        return slash >= 0 ? normalized.substring(0, slash) : normalized;
    }
}
