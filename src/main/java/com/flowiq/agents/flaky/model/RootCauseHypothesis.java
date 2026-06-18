package com.flowiq.agents.flaky.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RootCauseHypothesis {
    RootCauseType type;
    String description;
    double confidence;
}
