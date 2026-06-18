package com.flowiq.agents.gap.analyzer;

import com.flowiq.agents.gap.model.EndpointCoverage;
import com.flowiq.agents.gap.model.GapType;
import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;

import java.util.ArrayList;
import java.util.List;

public class EndpointCoverageGapAnalyzer implements GapAnalyzer {

    private final BusinessImpactPrioritizer prioritizer;

    public EndpointCoverageGapAnalyzer(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    @Override
    public String name() {
        return "EndpointCoverageGapAnalyzer";
    }

    @Override
    public List<TestGap> analyze(GapAnalysisContext context) {
        List<TestGap> gaps = new ArrayList<>();
        for (EndpointCoverage coverage : context.getEndpointCoverages()) {
            boolean uiExpected = prioritizer.uiExpected(coverage.getModule());

            if (!coverage.hasAnyCoverage()) {
                gaps.add(buildGap(coverage, GapType.NO_TEST_COVERAGE,
                        "No automated tests reference " + coverage.getMethod() + " " + coverage.getPath(),
                        recommend(coverage, "RegressionTest")));
                continue;
            }
            if (!coverage.isContractCovered()) {
                gaps.add(buildGap(coverage, GapType.MISSING_CONTRACT_COVERAGE,
                        "Missing contract test for " + coverage.getMethod() + " " + coverage.getPath(),
                        recommend(coverage, "ContractTest")));
            }
            if (!coverage.isSmokeCovered()) {
                gaps.add(buildGap(coverage, GapType.MISSING_SMOKE_COVERAGE,
                        "Missing smoke test for " + coverage.getMethod() + " " + coverage.getPath(),
                        recommend(coverage, "SmokeApiTest")));
            }
            if (!coverage.isRegressionCovered()) {
                gaps.add(buildGap(coverage, GapType.MISSING_REGRESSION_COVERAGE,
                        "Missing regression test for " + coverage.getMethod() + " " + coverage.getPath(),
                        recommend(coverage, "RegressionTest")));
            }
            if (uiExpected && !coverage.isUiCovered()) {
                gaps.add(buildGap(coverage, GapType.MISSING_UI_COVERAGE,
                        "Missing UI smoke test for module " + coverage.getModule(),
                        recommend(coverage, "UiSmokeTest")));
            }
        }
        return gaps;
    }

    private TestGap buildGap(EndpointCoverage coverage, GapType type, String description, String recommended) {
        return prioritizer.prioritize(TestGap.builder()
                .type(type)
                .module(coverage.getModule())
                .path(coverage.getPath())
                .method(coverage.getMethod())
                .description(description)
                .recommendedTest(recommended)
                .build());
    }

    private String recommend(EndpointCoverage coverage, String suffix) {
        String moduleClass = toClassPrefix(coverage.getModule());
        return moduleClass + suffix + " — cover " + coverage.getMethod() + " " + coverage.getPath();
    }

    private static String toClassPrefix(String module) {
        StringBuilder builder = new StringBuilder();
        for (String part : module.split("-")) {
            if (part.isEmpty()) continue;
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
