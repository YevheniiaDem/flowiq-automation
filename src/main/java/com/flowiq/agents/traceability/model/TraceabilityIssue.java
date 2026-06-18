package com.flowiq.agents.traceability.model;

import com.flowiq.agents.gap.model.GapSeverity;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TraceabilityIssue {
    TraceabilityIssueType type;
    String module;
    String featureName;
    GapSeverity severity;
    String description;
}
