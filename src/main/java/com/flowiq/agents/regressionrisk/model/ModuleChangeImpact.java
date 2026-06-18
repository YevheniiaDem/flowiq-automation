package com.flowiq.agents.regressionrisk.model;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.model.ApiChange;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ModuleChangeImpact {
    String module;
    boolean backendChanged;
    boolean frontendChanged;
    int apiChangeCount;
  @Singular("apiChange")
    List<ApiChange> apiChanges;
    AffectedTests allAffectedTests;
    AffectedTests selectedTests;
    GapSeverity risk;
    String recommendedRegressionScope;
    int estimatedExecutionMinutes;
}
