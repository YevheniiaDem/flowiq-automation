package com.flowiq.agents.flaky.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FlakyTestFinding {
    TestStabilityMetrics metrics;
    RootCauseHypothesis primaryRootCause;
  @Singular
    List<RootCauseHypothesis> alternateCauses;
    String recommendedFix;
    double priorityScore;
    int ciFailureCount;
    String lastFailureMessage;
}
