package com.flowiq.agents.gap.analyzer;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapType;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;

import java.util.ArrayList;
import java.util.List;

public class NegativeScenarioGapAnalyzer implements GapAnalyzer {

    private final BusinessImpactPrioritizer prioritizer;

    public NegativeScenarioGapAnalyzer(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    @Override
    public String name() {
        return "NegativeScenarioGapAnalyzer";
    }

    @Override
    public List<TestGap> analyze(GapAnalysisContext context) {
        List<TestGap> gaps = new ArrayList<>();
        for (EndpointCoverage coverage : context.getEndpointCoverages()) {
            if (!coverage.isRegressionCovered()) {
                continue;
            }
            if (coverage.isNegativeCovered()) {
                continue;
            }
            if ("GET".equals(coverage.getMethod()) && coverage.getPath().endsWith("/health")) {
                continue;
            }
            gaps.add(prioritizer.prioritize(TestGap.builder()
                    .type(GapType.MISSING_NEGATIVE_SCENARIO)
                    .module(coverage.getModule())
                    .path(coverage.getPath())
                    .method(coverage.getMethod())
                    .description("No negative scenario tests (invalid input, 404, validation) for "
                            + coverage.getMethod() + " " + coverage.getPath())
                    .recommendedTest(toClassPrefix(coverage.getModule()) + "RegressionTest — add invalid/not-found test for "
                            + coverage.getMethod() + " " + coverage.getPath())
                    .build()));
        }
        return gaps;
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
