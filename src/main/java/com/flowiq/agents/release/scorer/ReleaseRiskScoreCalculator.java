package com.flowiq.agents.release.scorer;

import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.model.ApiChangeReportInsight;
import com.flowiq.agents.release.model.CriticalFailure;
import com.flowiq.agents.release.model.FailureSeverity;
import com.flowiq.agents.release.model.FlakyReportInsight;
import com.flowiq.agents.release.model.ReleaseRecommendation;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import com.flowiq.agents.release.model.SuiteExecutionSummary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReleaseRiskScoreCalculator {

    private final ReleaseRiskAgentConfig config;

    public ReleaseRiskScoreCalculator(ReleaseRiskAgentConfig config) {
        this.config = config;
    }

    public ScoreResult calculate(SuiteExecutionSummary regression,
                                 SuiteExecutionSummary smoke,
                                 SuiteExecutionSummary contract,
                                 FlakyReportInsight flaky,
                                 ApiChangeReportInsight apiChange,
                                 List<CriticalFailure> criticalFailures) {
        Map<String, Double> breakdown = new LinkedHashMap<>();

        double regressionRisk = suiteRisk(regression) * config.weightRegression() / 100.0;
        double smokeRisk = suiteRisk(smoke) * config.weightSmoke() / 100.0;
        double contractRisk = suiteRisk(contract) * config.weightContract() / 100.0;
        double flakyRisk = flakyRisk(flaky) * config.weightFlaky() / 100.0;
        double apiRisk = apiChangeRisk(apiChange) * config.weightApiChange() / 100.0;

        breakdown.put("regression", round(regressionRisk));
        breakdown.put("smoke", round(smokeRisk));
        breakdown.put("contract", round(contractRisk));
        breakdown.put("flaky", round(flakyRisk));
        breakdown.put("apiChange", round(apiRisk));

        double score = Math.min(100.0, regressionRisk + smokeRisk + contractRisk + flakyRisk + apiRisk);

        long blockers = criticalFailures.stream()
                .filter(f -> f.getSeverity() == FailureSeverity.BLOCKER)
                .count();
        if (blockers > 0) {
            score = Math.max(score, config.yellowScoreMax() + 1);
        }
        if (apiChange.getBreakingChanges() >= 3) {
            score = Math.max(score, 50.0);
        }

        score = round(score);
        ReleaseRiskCategory category = categorize(score);
        ReleaseRecommendation recommendation = recommend(score, category, regression, smoke, contract,
                criticalFailures, apiChange, flaky);

        return new ScoreResult(score, category, recommendation, breakdown);
    }

    private double suiteRisk(SuiteExecutionSummary summary) {
        if (summary.getTotalTests() == 0) {
            return 50.0;
        }
        return 100.0 - summary.getPassRate();
    }

    private double flakyRisk(FlakyReportInsight flaky) {
        if (!flaky.isReportFound()) {
            return 10.0;
        }
        if (flaky.getFlakyTestCount() == 0) {
            return 0.0;
        }
        return Math.min(100.0, flaky.getPortfolioFlakinessPercent() + flaky.getFlakyTestCount() * 5.0);
    }

    private double apiChangeRisk(ApiChangeReportInsight apiChange) {
        if (!apiChange.isReportFound()) {
            return 20.0;
        }
        return switch (apiChange.getRiskLevel()) {
            case HIGH -> 100.0;
            case MEDIUM -> 60.0 + apiChange.getBreakingChanges() * 10.0;
            case LOW -> apiChange.getTotalChanges() > 0 ? 20.0 : 0.0;
        };
    }

    private ReleaseRiskCategory categorize(double score) {
        if (score <= config.greenScoreMax()) {
            return ReleaseRiskCategory.GREEN;
        }
        if (score <= config.yellowScoreMax()) {
            return ReleaseRiskCategory.YELLOW;
        }
        return ReleaseRiskCategory.RED;
    }

    private ReleaseRecommendation recommend(double score,
                                            ReleaseRiskCategory category,
                                            SuiteExecutionSummary regression,
                                            SuiteExecutionSummary smoke,
                                            SuiteExecutionSummary contract,
                                            List<CriticalFailure> criticalFailures,
                                            ApiChangeReportInsight apiChange,
                                            FlakyReportInsight flaky) {
        boolean smokeGateFailed = smoke.getTotalTests() > 0
                && smoke.getPassRate() < config.smokeMinPassRate();
        boolean regressionGateFailed = regression.getTotalTests() > 0
                && regression.getPassRate() < config.regressionMinPassRate();
        boolean contractGateFailed = contract.getTotalTests() > 0
                && contract.getPassRate() < config.contractMinPassRate();

        boolean hasSmokeBlocker = criticalFailures.stream()
                .anyMatch(f -> f.getSuiteType() == TestSuiteType.SMOKE);

        if (hasSmokeBlocker || smokeGateFailed || regressionGateFailed || category == ReleaseRiskCategory.RED) {
            return ReleaseRecommendation.DO_NOT_RELEASE;
        }
        if (contractGateFailed || category == ReleaseRiskCategory.YELLOW
                || apiChange.getRiskLevel() == RiskLevel.HIGH
                || (flaky.isReportFound() && flaky.getFlakyTestCount() > 3)
                || apiChange.getBreakingChanges() > 0) {
            return ReleaseRecommendation.APPROVE_WITH_RISK;
        }
        return ReleaseRecommendation.APPROVE_RELEASE;
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record ScoreResult(
            double score,
            ReleaseRiskCategory category,
            ReleaseRecommendation recommendation,
            Map<String, Double> breakdown
    ) {
    }
}
