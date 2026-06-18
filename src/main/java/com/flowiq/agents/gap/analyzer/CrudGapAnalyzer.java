package com.flowiq.agents.gap.analyzer;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapType;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.TestSourceScanner;

import java.util.ArrayList;
import java.util.List;

public class CrudGapAnalyzer implements GapAnalyzer {

    private final BusinessImpactPrioritizer prioritizer;

    public CrudGapAnalyzer(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    @Override
    public String name() {
        return "CrudGapAnalyzer";
    }

    @Override
    public List<TestGap> analyze(GapAnalysisContext context) {
        List<TestGap> gaps = new ArrayList<>();

        for (EndpointCoverage coverage : context.getEndpointCoverages()) {
            String base = resourceBase(coverage.getPath());
            boolean resourceHasCreate = context.getEndpointCoverages().stream()
                    .anyMatch(e -> resourceBase(e.getPath()).equals(base) && "POST".equals(e.getMethod()));

            if (!resourceHasCreate) {
                continue;
            }

            if (isUpdateMethod(coverage.getMethod()) && !coverage.isRegressionCovered()) {
                gaps.add(prioritizer.prioritize(TestGap.builder()
                        .type(GapType.MISSING_UPDATE_TEST)
                        .module(coverage.getModule())
                        .path(coverage.getPath())
                        .method(coverage.getMethod())
                        .description("CRUD resource " + base + " exposes update but has no regression test for "
                                + coverage.getMethod() + " " + coverage.getPath())
                        .recommendedTest(toClassPrefix(coverage.getModule()) + "RegressionTest — add shouldUpdate* for "
                                + coverage.getPath())
                        .build()));
            }

            if ("DELETE".equals(coverage.getMethod()) && !coverage.isRegressionCovered()) {
                gaps.add(prioritizer.prioritize(TestGap.builder()
                        .type(GapType.MISSING_DELETE_TEST)
                        .module(coverage.getModule())
                        .path(coverage.getPath())
                        .method(coverage.getMethod())
                        .description("CRUD resource " + base + " exposes delete but has no regression test for "
                                + coverage.getPath())
                        .recommendedTest(toClassPrefix(coverage.getModule()) + "RegressionTest — add shouldDelete* for "
                                + coverage.getPath())
                        .build()));
            }
        }
        return gaps;
    }

    private static boolean isUpdateMethod(String method) {
        return "PUT".equals(method) || "PATCH".equals(method);
    }

    private static String resourceBase(String path) {
        return TestSourceScanner.normalizePath(path).replaceAll("/\\{[^}]+}.*", "");
    }

    private static String toClassPrefix(String module) {
        StringBuilder builder = new StringBuilder();
        for (String part : module.split("-")) {
            if (part.isEmpty()) continue;
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) builder.append(part.substring(1));
        }
        return builder.toString();
    }
}
