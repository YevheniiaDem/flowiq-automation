package com.flowiq.agents.factory.scorer;

import com.flowiq.agents.factory.model.FactoryAgentResultsBundle;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.factory.model.PrIntelligenceVerdict;
import com.flowiq.agents.model.AnalysisResult;
import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.prreview.model.PrReviewVerdict;
import com.flowiq.agents.prreview.model.PullRequestReviewResult;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;
import com.flowiq.agents.regressionrisk.model.RiskBasedRegressionResult;
import com.flowiq.agents.review.model.ReviewVerdict;
import com.flowiq.agents.review.model.TestReviewResult;

import java.util.ArrayList;
import java.util.List;

public class PrIntelligenceScorer {

    public int calculateScore(FactoryAgentResultsBundle bundle) {
        int score = 100;

        var prReview = bundle.payload(FactoryAgentType.PULL_REQUEST_REVIEW, PullRequestReviewResult.class);
        if (prReview.isPresent()) {
            PullRequestReviewResult result = prReview.get();
            score -= switch (result.getVerdict()) {
                case REJECTED -> 30;
                case APPROVED_WITH_RISK -> 12;
                case APPROVED -> 0;
            };
            score -= Math.min(20, result.getCriticalFindings() * 5 + result.getHighFindings() * 2);
        } else {
            score -= 25;
        }

        var testReview = bundle.payload(FactoryAgentType.TEST_REVIEW, TestReviewResult.class);
        if (testReview.isPresent()) {
            TestReviewResult result = testReview.get();
            score -= switch (result.getOverallVerdict()) {
                case REJECTED -> 25;
                case APPROVED_WITH_RISK -> 10;
                case APPROVED -> 0;
            };
            score -= Math.min(15, result.getRejectedCount() * 5);
        } else {
            score -= 20;
        }

        var apiChange = bundle.payload(FactoryAgentType.API_CHANGE_DETECTION, AnalysisResult.class);
        if (apiChange.isPresent()) {
            AnalysisResult result = apiChange.get();
            score -= switch (result.getRiskLevel()) {
                case HIGH -> 25;
                case MEDIUM -> 12;
                case LOW -> result.getChanges().isEmpty() ? 0 : 4;
            };
            if (result.getChanges().stream().anyMatch(c -> c.isBreaking())) {
                score -= 10;
            }
        } else {
            score -= 15;
        }

        var regression = bundle.payload(FactoryAgentType.RISK_BASED_REGRESSION, RiskBasedRegressionResult.class);
        if (regression.isPresent()) {
            score -= switch (regression.get().getRecommendation()) {
                case FULL_REGRESSION -> 15;
                case PARTIAL_REGRESSION -> 6;
                case SMOKE_ONLY -> 0;
            };
        }

        return clamp(score);
    }

    public PrIntelligenceVerdict determineVerdict(FactoryAgentResultsBundle bundle, int score) {
        PrIntelligenceVerdict verdict = PrIntelligenceVerdict.APPROVED;

        var prReview = bundle.payload(FactoryAgentType.PULL_REQUEST_REVIEW, PullRequestReviewResult.class);
        if (prReview.isPresent()) {
            verdict = PrIntelligenceVerdict.worst(verdict, mapPrReview(prReview.get().getVerdict()));
        }

        var testReview = bundle.payload(FactoryAgentType.TEST_REVIEW, TestReviewResult.class);
        if (testReview.isPresent()) {
            verdict = PrIntelligenceVerdict.worst(verdict, mapTestReview(testReview.get().getOverallVerdict()));
        }

        var apiChange = bundle.payload(FactoryAgentType.API_CHANGE_DETECTION, AnalysisResult.class);
        if (apiChange.isPresent() && apiChange.get().getRiskLevel() == RiskLevel.HIGH) {
            verdict = PrIntelligenceVerdict.worst(verdict, PrIntelligenceVerdict.APPROVED_WITH_RISK);
        }

        if (score < 50) {
            verdict = PrIntelligenceVerdict.REJECTED;
        } else if (score < 75 && verdict == PrIntelligenceVerdict.APPROVED) {
            verdict = PrIntelligenceVerdict.APPROVED_WITH_RISK;
        }

        return verdict;
    }

    public List<String> topRisks(FactoryAgentResultsBundle bundle) {
        List<String> risks = new ArrayList<>();
        bundle.payload(FactoryAgentType.PULL_REQUEST_REVIEW, PullRequestReviewResult.class)
                .ifPresent(r -> {
                    if (r.getCriticalFindings() > 0) {
                        risks.add("PR review: " + r.getCriticalFindings() + " critical finding(s)");
                    }
                    if (r.getVerdict() == PrReviewVerdict.REJECTED) {
                        risks.add("Pull request review verdict: REJECTED");
                    }
                });
        bundle.payload(FactoryAgentType.TEST_REVIEW, TestReviewResult.class)
                .ifPresent(r -> {
                    if (r.getOverallVerdict() == ReviewVerdict.REJECTED) {
                        risks.add("Test review rejected " + r.getRejectedCount() + " feature(s)");
                    }
                });
        bundle.payload(FactoryAgentType.API_CHANGE_DETECTION, AnalysisResult.class)
                .ifPresent(r -> {
                    if (r.getRiskLevel() == RiskLevel.HIGH) {
                        risks.add("High-risk API contract changes detected");
                    }
                });
        bundle.payload(FactoryAgentType.RISK_BASED_REGRESSION, RiskBasedRegressionResult.class)
                .ifPresent(r -> {
                    if (r.getRecommendation() == RegressionScopeRecommendation.FULL_REGRESSION) {
                        risks.add("Full regression scope required for current change set");
                    }
                });
        return risks;
    }

    public List<String> recommendedActions(FactoryAgentResultsBundle bundle, PrIntelligenceVerdict verdict) {
        List<String> actions = new ArrayList<>();
        if (verdict == PrIntelligenceVerdict.REJECTED) {
            actions.add("Resolve blocking PR and test coverage issues before merge");
        }
        bundle.payload(FactoryAgentType.TEST_REVIEW, TestReviewResult.class)
                .ifPresent(r -> {
                    if (r.getRejectedCount() > 0) {
                        actions.add("Add missing smoke/regression/contract tests for rejected features");
                    }
                });
        bundle.payload(FactoryAgentType.API_CHANGE_DETECTION, AnalysisResult.class)
                .ifPresent(r -> {
                    if (!r.getChanges().isEmpty()) {
                        actions.add("Review API change report and update affected contract tests");
                    }
                });
        if (actions.isEmpty()) {
            actions.add("PR quality signals are within acceptable thresholds");
        }
        return actions;
    }

    private static PrIntelligenceVerdict mapPrReview(PrReviewVerdict verdict) {
        return switch (verdict) {
            case REJECTED -> PrIntelligenceVerdict.REJECTED;
            case APPROVED_WITH_RISK -> PrIntelligenceVerdict.APPROVED_WITH_RISK;
            case APPROVED -> PrIntelligenceVerdict.APPROVED;
        };
    }

    private static PrIntelligenceVerdict mapTestReview(ReviewVerdict verdict) {
        return switch (verdict) {
            case REJECTED -> PrIntelligenceVerdict.REJECTED;
            case APPROVED_WITH_RISK -> PrIntelligenceVerdict.APPROVED_WITH_RISK;
            case APPROVED -> PrIntelligenceVerdict.APPROVED;
        };
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
