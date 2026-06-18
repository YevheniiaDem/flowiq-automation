package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MaintenanceFinding {
    MaintenanceFindingType type;
    MaintenanceSeverity severity;
    String title;
    String location;
    String recommendation;
    int priorityRank;
}
