package com.flowiq.agents.gap.model;

import com.flowiq.agents.model.TestSuiteType;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.EnumSet;
import java.util.Set;

@Value
@Builder
public class EndpointCoverage {
    String path;
    String method;
    String module;
    boolean contractCovered;
    boolean smokeCovered;
    boolean regressionCovered;
    boolean uiCovered;
    boolean negativeCovered;
    boolean authorizationCovered;
    boolean requiresAuth;
  @Singular
    Set<String> coveringTests;

    public boolean hasAnyCoverage() {
        return contractCovered || smokeCovered || regressionCovered || uiCovered;
    }

    public double coverageScore(boolean uiExpected) {
        int total = uiExpected ? 4 : 3;
        int covered = 0;
        if (contractCovered) covered++;
        if (smokeCovered) covered++;
        if (regressionCovered) covered++;
        if (uiExpected && uiCovered) covered++;
        return total == 0 ? 0.0 : (covered * 100.0) / total;
    }

    public Set<TestSuiteType> missingSuites(boolean uiExpected) {
        Set<TestSuiteType> missing = EnumSet.noneOf(TestSuiteType.class);
        if (!contractCovered) missing.add(TestSuiteType.CONTRACT);
        if (!smokeCovered) missing.add(TestSuiteType.SMOKE);
        if (!regressionCovered) missing.add(TestSuiteType.REGRESSION);
        if (uiExpected && !uiCovered) missing.add(TestSuiteType.UI);
        return missing;
    }
}
