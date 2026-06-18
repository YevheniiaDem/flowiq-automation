package com.flowiq.agents.gap.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ModuleCoverage {
    String module;
    GapSeverity businessImpact;
    int totalEndpoints;
    int coveredEndpoints;
    double coveragePercent;
  @Singular
    List<EndpointCoverage> endpoints;
  @Singular
    List<TestGap> gaps;
}
