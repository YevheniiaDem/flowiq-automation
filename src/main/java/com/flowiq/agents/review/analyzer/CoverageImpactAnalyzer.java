package com.flowiq.agents.review.analyzer;

import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.gap.scanner.EndpointMatcher;
import com.flowiq.agents.gap.scanner.ScannedTestReference;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.review.model.CoverageStatus;
import com.flowiq.agents.review.model.FeatureChange;
import com.flowiq.agents.review.model.FeatureChangeType;

import java.util.List;

public class CoverageImpactAnalyzer {

    private final BusinessImpactPrioritizer prioritizer;

    public CoverageImpactAnalyzer(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    public CoverageStatus analyze(FeatureChange feature, List<ScannedTestReference> tests) {
        String module = feature.getModule();
        List<ScannedTestReference> moduleTests = tests.stream()
                .filter(t -> module.equals(t.getModule()))
                .toList();

        boolean smoke = hasSuite(moduleTests, TestSuiteType.SMOKE);
        boolean regression = hasSuite(moduleTests, TestSuiteType.REGRESSION);
        boolean contract = hasSuite(moduleTests, TestSuiteType.CONTRACT);
        boolean ui = hasSuite(moduleTests, TestSuiteType.UI);

        if (feature.getChangeType() == FeatureChangeType.ENDPOINT) {
            List<ScannedTestReference> endpointTests = tests.stream()
                    .filter(t -> EndpointMatcher.matches(t.getPath(), feature.getEndpointPath()))
                    .filter(t -> EndpointMatcher.methodMatches(t.getMethod(), feature.getHttpMethod()))
                    .toList();
            if (!endpointTests.isEmpty()) {
                smoke = smoke || hasSuite(endpointTests, TestSuiteType.SMOKE);
                regression = regression || hasSuite(endpointTests, TestSuiteType.REGRESSION);
                contract = contract || hasSuite(endpointTests, TestSuiteType.CONTRACT);
            }
        }

        boolean positive = moduleTests.stream().anyMatch(t -> !t.isNegativeScenario());
        boolean negative = moduleTests.stream().anyMatch(ScannedTestReference::isNegativeScenario);
        boolean authorization = moduleTests.stream().anyMatch(ScannedTestReference::isAuthorizationCheck);

        if (prioritizer.uiExpected(module)) {
            ui = ui || hasSuite(moduleTests, TestSuiteType.UI);
        }

        return CoverageStatus.builder()
                .smokeCovered(smoke)
                .regressionCovered(regression)
                .contractCovered(contract)
                .uiCovered(ui)
                .positiveCovered(positive || smoke || regression || contract)
                .negativeCovered(negative)
                .authorizationCovered(authorization)
                .build();
    }

    public boolean uiExpected(String module) {
        return prioritizer.uiExpected(module);
    }

    private static boolean hasSuite(List<ScannedTestReference> tests, TestSuiteType suite) {
        return tests.stream().anyMatch(t -> t.getSuites().contains(suite));
    }
}
