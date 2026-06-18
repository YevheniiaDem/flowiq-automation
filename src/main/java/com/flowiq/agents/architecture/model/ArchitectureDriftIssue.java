package com.flowiq.agents.architecture.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ArchitectureDriftIssue {
    DriftIssueType type;
    String issue;
    DriftSeverity severity;
    String location;
    String recommendation;
}
