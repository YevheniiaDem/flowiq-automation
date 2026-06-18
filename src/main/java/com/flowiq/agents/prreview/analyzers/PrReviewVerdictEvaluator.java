package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.model.PrReviewSeverity;
import com.flowiq.agents.prreview.model.PrReviewVerdict;

import java.util.List;

public class PrReviewVerdictEvaluator {

    public PrReviewVerdict evaluate(List<PrReviewFinding> findings) {
        if (findings.isEmpty()) {
            return PrReviewVerdict.APPROVED;
        }
        if (findings.stream().anyMatch(f -> f.getSeverity() == PrReviewSeverity.CRITICAL)
                || findings.stream().anyMatch(PrReviewFinding::isBlocking)) {
            return PrReviewVerdict.REJECTED;
        }
        if (findings.stream().anyMatch(f -> f.getSeverity() == PrReviewSeverity.HIGH
                || f.getSeverity() == PrReviewSeverity.MEDIUM)) {
            return PrReviewVerdict.APPROVED_WITH_RISK;
        }
        return PrReviewVerdict.APPROVED;
    }

    public String buildRecommendation(PrReviewVerdict verdict, List<PrReviewFinding> findings) {
        return switch (verdict) {
            case REJECTED -> "Do not merge until blocking QA/Architecture findings are resolved. "
                    + "Address " + countBlocking(findings) + " blocking issue(s) first.";
            case APPROVED_WITH_RISK -> "Merge allowed with documented test/architecture debt. "
                    + "Resolve " + findings.size() + " finding(s) in a follow-up before release.";
            case APPROVED -> "Pull request meets QA and architecture quality gates for pre-test review.";
        };
    }

    private static long countBlocking(List<PrReviewFinding> findings) {
        return findings.stream().filter(PrReviewFinding::isBlocking).count();
    }
}
