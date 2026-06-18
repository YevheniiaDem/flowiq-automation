package com.flowiq.agents.review.analyzer;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.review.config.TestReviewAgentConfig;
import com.flowiq.agents.review.model.CoverageStatus;
import com.flowiq.agents.review.model.FeatureChange;
import com.flowiq.agents.review.model.FeatureChangeType;
import com.flowiq.agents.review.model.FeatureReviewItem;
import com.flowiq.agents.review.model.ReviewVerdict;

import java.util.ArrayList;
import java.util.List;

public class MissingTestAnalyzer {

    private final TestReviewAgentConfig config;
    private final BusinessImpactPrioritizer prioritizer;
    private final CoverageImpactAnalyzer coverageAnalyzer;

    public MissingTestAnalyzer(TestReviewAgentConfig config,
                               BusinessImpactPrioritizer prioritizer,
                               CoverageImpactAnalyzer coverageAnalyzer) {
        this.config = config;
        this.prioritizer = prioritizer;
        this.coverageAnalyzer = coverageAnalyzer;
    }

    public FeatureReviewItem analyze(FeatureChange feature, CoverageStatus coverage) {
        List<String> missing = new ArrayList<>();
        boolean uiExpected = coverageAnalyzer.uiExpected(feature.getModule());

        if (!coverage.isSmokeCovered()) {
            missing.add("Smoke test coverage");
        }
        if (!coverage.isRegressionCovered()) {
            missing.add("Regression test coverage");
        }
        if (!coverage.isContractCovered() && feature.getChangeType() != FeatureChangeType.SERVICE) {
            missing.add("Contract test coverage");
        }
        if (uiExpected && !coverage.isUiCovered()) {
            missing.add("UI smoke test coverage");
        }
        if (!coverage.isPositiveCovered()) {
            missing.add("Positive scenario tests");
        }
        if (!coverage.isNegativeCovered()) {
            missing.add("Negative scenario tests");
        }
        if (!coverage.isAuthorizationCovered() && requiresAuth(feature)) {
            missing.add("Authorization tests");
        }

        GapSeverity risk = prioritizer.businessImpactFor(feature.getModule());
        if (!missing.isEmpty() && risk.ordinal() > GapSeverity.MEDIUM.ordinal()) {
            risk = GapSeverity.HIGH;
        }
        if (missing.size() >= 4) {
            risk = GapSeverity.max(risk, GapSeverity.CRITICAL);
        }

        ReviewVerdict verdict = determineVerdict(feature, coverage, missing, uiExpected);
        String recommendation = buildRecommendation(feature, missing, verdict);

        return FeatureReviewItem.builder()
                .feature(feature)
                .coverageStatus(coverage)
                .missingTests(missing)
                .risk(risk)
                .recommendation(recommendation)
                .verdict(verdict)
                .build();
    }

    private ReviewVerdict determineVerdict(FeatureChange feature,
                                         CoverageStatus coverage,
                                         List<String> missing,
                                         boolean uiExpected) {
        if (feature.getChangeType() == FeatureChangeType.ENDPOINT) {
            if (config.rejectIfNoContractOnNewEndpoint() && !coverage.isContractCovered()) {
                return ReviewVerdict.REJECTED;
            }
            if (config.rejectIfNoRegression() && !coverage.isRegressionCovered()) {
                return ReviewVerdict.REJECTED;
            }
        }
        if (missing.isEmpty()) {
            return ReviewVerdict.APPROVED;
        }
        if (missing.contains("Regression test coverage") || missing.contains("Contract test coverage")) {
            return ReviewVerdict.REJECTED;
        }
        if (missing.size() >= 3 || (uiExpected && missing.contains("UI smoke test coverage"))) {
            return ReviewVerdict.APPROVED_WITH_RISK;
        }
        return ReviewVerdict.APPROVED_WITH_RISK;
    }

    private static boolean requiresAuth(FeatureChange feature) {
        if (feature.getChangeType() != FeatureChangeType.ENDPOINT) {
            return true;
        }
        String path = feature.getEndpointPath();
        return path != null && !path.startsWith("/auth/login") && !path.startsWith("/auth/register");
    }

    private static String buildRecommendation(FeatureChange feature,
                                              List<String> missing,
                                              ReviewVerdict verdict) {
        if (verdict == ReviewVerdict.APPROVED) {
            return "Test coverage is sufficient for this feature change.";
        }
        String module = feature.getModule();
        StringBuilder rec = new StringBuilder();
        if (missing.contains("Contract test coverage")) {
            rec.append("Add/update ").append(module).append(" contract tests and JSON schemas. ");
        }
        if (missing.contains("Regression test coverage")) {
            rec.append("Extend ").append(module).append(" regression suite for new behavior. ");
        }
        if (missing.contains("Smoke test coverage")) {
            rec.append("Add API smoke test in ").append(module).append("SmokeApiTest. ");
        }
        if (missing.contains("UI smoke test coverage")) {
            rec.append("Add UI smoke coverage in ").append(module).append("UiSmokeTest. ");
        }
        if (missing.contains("Negative scenario tests")) {
            rec.append("Add negative/unauthorized scenarios. ");
        }
        if (missing.contains("Authorization tests")) {
            rec.append("Add 401/403 authorization checks. ");
        }
        if (verdict == ReviewVerdict.REJECTED) {
            rec.append("Block merge until mandatory suites are added.");
        } else {
            rec.append("Merge allowed with documented test debt.");
        }
        return rec.toString().trim();
    }
}
