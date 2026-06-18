package com.flowiq.agents.regressionrisk.scorer;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.regressionrisk.config.RegressionRiskAgentConfig;
import com.flowiq.agents.regressionrisk.model.ModuleChangeImpact;
import com.flowiq.agents.regressionrisk.model.RegressionScopeRecommendation;

import java.util.List;

public class RegressionRiskScorer {

    private final RegressionRiskAgentConfig config;

    public RegressionRiskScorer(RegressionRiskAgentConfig config) {
        this.config = config;
    }

    public RegressionScopeRecommendation recommend(List<ModuleChangeImpact> modulePlans,
                                                   List<ApiChange> apiChanges) {
        if (modulePlans.isEmpty()) {
            return RegressionScopeRecommendation.SMOKE_ONLY;
        }

        long critical = modulePlans.stream().filter(p -> p.getRisk() == GapSeverity.CRITICAL).count();
        long high = modulePlans.stream().filter(p -> p.getRisk() == GapSeverity.HIGH).count();
        boolean breaking = apiChanges.stream().anyMatch(ApiChange::isBreaking);

        if (breaking || critical >= config.fullRegressionCriticalModules()) {
            return RegressionScopeRecommendation.FULL_REGRESSION;
        }
        if (high >= config.partialRegressionHighModules()
                || modulePlans.stream().anyMatch(p -> p.getRisk() == GapSeverity.HIGH)) {
            return RegressionScopeRecommendation.PARTIAL_REGRESSION;
        }
        if (modulePlans.stream().allMatch(p -> p.getRisk() == GapSeverity.LOW)) {
            return RegressionScopeRecommendation.SMOKE_ONLY;
        }
        return RegressionScopeRecommendation.PARTIAL_REGRESSION;
    }

    public int totalExecutionMinutes(List<ModuleChangeImpact> modulePlans) {
        return modulePlans.stream().mapToInt(ModuleChangeImpact::getEstimatedExecutionMinutes).sum();
    }

    public int totalSelectedTestClasses(List<ModuleChangeImpact> modulePlans) {
        return modulePlans.stream()
                .mapToInt(p -> p.getSelectedTests().totalTestClasses())
                .sum();
    }
}
