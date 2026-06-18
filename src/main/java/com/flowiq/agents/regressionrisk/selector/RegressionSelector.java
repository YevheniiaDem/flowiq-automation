package com.flowiq.agents.regressionrisk.selector;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.regressionrisk.analyzer.ChangeImpactAnalyzer;
import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.AffectedTests;
import com.flowiq.agents.regressionrisk.model.ModuleChangeImpact;

import java.util.ArrayList;
import java.util.List;

public class RegressionSelector {

    private final RegressionRiskAgentConfig config;

    public RegressionSelector(RegressionRiskAgentConfig config) {
        this.config = config;
    }

    public ModuleChangeImpact select(ChangeImpactAnalyzer.ModuleImpactDraft draft, GapSeverity risk) {
        AffectedTests all = draft.getAllAffectedTests();
        AffectedTests selected = selectByRisk(all, draft.isFrontendChanged(), risk);
        String scope = describeScope(selected, risk);
        int minutes = estimateMinutes(selected);

        return ModuleChangeImpact.builder()
                .module(draft.getModule())
                .backendChanged(draft.isBackendChanged())
                .frontendChanged(draft.isFrontendChanged())
                .apiChangeCount(draft.getApiChanges().size())
                .apiChanges(draft.getApiChanges())
                .allAffectedTests(all)
                .selectedTests(selected)
                .risk(risk)
                .recommendedRegressionScope(scope)
                .estimatedExecutionMinutes(minutes)
                .build();
    }

    private AffectedTests selectByRisk(AffectedTests all, boolean frontendChanged, GapSeverity risk) {
        return switch (risk) {
            case CRITICAL -> all;
            case HIGH -> AffectedTests.builder()
                    .smokeTests(all.getSmokeTests())
                    .contractTests(all.getContractTests())
                    .regressionTests(all.getRegressionTests())
                    .uiTests(frontendChanged ? all.getUiTests() : List.of())
                    .build();
            case MEDIUM -> AffectedTests.builder()
                    .smokeTests(all.getSmokeTests())
                    .regressionTests(all.getRegressionTests())
                    .uiTests(frontendChanged ? all.getUiTests() : List.of())
                    .build();
            case LOW -> AffectedTests.builder()
                    .smokeTests(all.getSmokeTests())
                    .build();
        };
    }

    private int estimateMinutes(AffectedTests selected) {
        return selected.getSmokeTests().size() * config.minutesSmoke()
                + selected.getContractTests().size() * config.minutesContract()
                + selected.getRegressionTests().size() * config.minutesRegression()
                + selected.getUiTests().size() * config.minutesUi();
    }

    private static String describeScope(AffectedTests selected, GapSeverity risk) {
        List<String> parts = new ArrayList<>();
        if (!selected.getSmokeTests().isEmpty()) {
            parts.add("smoke");
        }
        if (!selected.getContractTests().isEmpty()) {
            parts.add("contract");
        }
        if (!selected.getRegressionTests().isEmpty()) {
            parts.add("regression");
        }
        if (!selected.getUiTests().isEmpty()) {
            parts.add("ui");
        }
        if (parts.isEmpty()) {
            return risk.name() + " — no mapped tests; run domain smoke manually";
        }
        return risk.name() + " — " + String.join(" + ", parts);
    }
}
