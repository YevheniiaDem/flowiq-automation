package com.flowiq.agents.review.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CoverageStatus {
    boolean smokeCovered;
    boolean regressionCovered;
    boolean contractCovered;
    boolean uiCovered;
    boolean positiveCovered;
    boolean negativeCovered;
    boolean authorizationCovered;

    public double coveragePercent(boolean uiExpected) {
        int total = uiExpected ? 7 : 6;
        int covered = 0;
        if (smokeCovered) covered++;
        if (regressionCovered) covered++;
        if (contractCovered) covered++;
        if (uiExpected && uiCovered) covered++;
        if (positiveCovered) covered++;
        if (negativeCovered) covered++;
        if (authorizationCovered) covered++;
        return (covered * 100.0) / total;
    }
}
