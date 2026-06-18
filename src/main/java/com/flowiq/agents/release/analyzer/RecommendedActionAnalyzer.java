package com.flowiq.agents.release.analyzer;

import com.flowiq.agents.model.RiskLevel;
import com.flowiq.agents.release.model.ApiChangeReportInsight;
import com.flowiq.agents.release.model.BlockedArea;
import com.flowiq.agents.release.model.CriticalFailure;
import com.flowiq.agents.release.model.FlakyReportInsight;
import com.flowiq.agents.release.model.ReleaseRecommendation;
import com.flowiq.agents.release.model.ReleaseRiskCategory;
import com.flowiq.agents.release.model.SuiteExecutionSummary;

import java.util.ArrayList;
import java.util.List;

public class RecommendedActionAnalyzer {

    public List<String> generate(SuiteExecutionSummary regression,
                                 SuiteExecutionSummary smoke,
                                 SuiteExecutionSummary contract,
                                 FlakyReportInsight flaky,
                                 ApiChangeReportInsight apiChange,
                                 List<CriticalFailure> criticalFailures,
                                 List<BlockedArea> blockedAreas,
                                 ReleaseRiskCategory category,
                                 ReleaseRecommendation recommendation) {
        List<String> actions = new ArrayList<>();

        if (smoke.hasFailures()) {
            actions.add("Fix all smoke test failures before release — smoke gates user-facing flows.");
            actions.add("Re-run: `mvn test -Papi-smoke -Pui-smoke -Plocal`");
        }
        if (contract.hasFailures()) {
            actions.add("Resolve contract test failures and update JSON schemas if API changed.");
            actions.add("Re-run: `mvn test -Pcontract -Plocal`");
        }
        if (regression.hasFailures()) {
            actions.add("Investigate regression failures in: "
                    + String.join(", ", blockedAreas.stream().map(BlockedArea::getModule).limit(5).toList()));
            actions.add("Re-run: `mvn test -Papi-regression -Plocal`");
        }
        if (flaky.isReportFound() && flaky.getFlakyTestCount() > 0) {
            actions.add("Stabilize " + flaky.getFlakyTestCount()
                    + " flaky test(s) identified in flaky-tests-report.md before expanding release scope.");
            if (!flaky.getTopUnstableTests().isEmpty()) {
                actions.add("Priority flaky tests: `" + String.join("`, `", flaky.getTopUnstableTests()) + "`");
            }
        }
        if (apiChange.isReportFound() && apiChange.getBreakingChanges() > 0) {
            actions.add("Review " + apiChange.getBreakingChanges()
                    + " breaking API change(s) with backend team; update contract schemas and affected tests.");
            actions.add("Re-run ApiChangeDetectionAgent and verify impact matrix in api-change-report.md");
        } else if (apiChange.isReportFound() && apiChange.getRiskLevel() != RiskLevel.LOW) {
            actions.add("Validate API changes at " + apiChange.getRiskLevel()
                    + " risk level against contract and regression suites.");
        }
        if (recommendation == ReleaseRecommendation.APPROVE_WITH_RISK) {
            actions.add("Document accepted risks in release notes and schedule post-release monitoring.");
            actions.add("Enable enhanced logging and rollback plan for affected modules.");
        }
        if (recommendation == ReleaseRecommendation.DO_NOT_RELEASE) {
            actions.add("Hold release until critical failures are resolved and all gate suites pass.");
        }
        if (category == ReleaseRiskCategory.GREEN && actions.isEmpty()) {
            actions.add("All gate suites green — proceed with standard release checklist.");
            actions.add("Archive Surefire/Allure results for audit trail.");
        }
        if (actions.isEmpty()) {
            actions.add("Monitor production metrics for 24h post-release.");
        }
        return actions;
    }
}
