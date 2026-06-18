package com.flowiq.agents.regressionrisk.analyzer;

import com.flowiq.agents.gap.config.TestGapAgentConfig;
import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import com.flowiq.agents.model.ApiChange;
import com.flowiq.agents.model.ChangeType;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

public class BusinessCriticalityAnalyzer {

    private final BusinessImpactPrioritizer prioritizer;

    public BusinessCriticalityAnalyzer() {
        this(new BusinessImpactPrioritizer(ConfigFactory.create(TestGapAgentConfig.class)));
    }

    public BusinessCriticalityAnalyzer(BusinessImpactPrioritizer prioritizer) {
        this.prioritizer = prioritizer;
    }

    public GapSeverity analyze(String module,
                               boolean backendChanged,
                               boolean frontendChanged,
                               List<ApiChange> apiChanges) {
        GapSeverity base = prioritizer.businessImpactFor(module);

        if (hasBreakingChange(apiChanges)) {
            return GapSeverity.CRITICAL;
        }
        if (apiChanges.stream().anyMatch(c -> c.getType() == ChangeType.ADDED_ENDPOINT)) {
            base = GapSeverity.max(base, GapSeverity.HIGH);
        }
        if (apiChanges.stream().anyMatch(c -> c.getType() == ChangeType.MODIFIED_RESPONSE_SCHEMA)) {
            base = GapSeverity.max(base, GapSeverity.HIGH);
        }
        if (frontendChanged && prioritizer.uiExpected(module)) {
            base = escalateForUi(base);
        }
        if (backendChanged && frontendChanged) {
            base = GapSeverity.max(base, GapSeverity.HIGH);
        }
        return base;
    }

    private static boolean hasBreakingChange(List<ApiChange> apiChanges) {
        return apiChanges.stream().anyMatch(ApiChange::isBreaking);
    }

    private static GapSeverity escalateForUi(GapSeverity base) {
        return switch (base) {
            case CRITICAL -> GapSeverity.CRITICAL;
            case HIGH -> GapSeverity.HIGH;
            case MEDIUM -> GapSeverity.MEDIUM;
            case LOW -> GapSeverity.MEDIUM;
        };
    }
}
