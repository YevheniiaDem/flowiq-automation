package com.flowiq.agents.gap.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestGap {
    GapType type;
    GapSeverity severity;
    String module;
    String path;
    String method;
    String description;
    String recommendedTest;
}
