package com.flowiq.agents.orchestrator.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class QualityDimensionSummary {
    String name;
    int healthScore;
  @Singular("highlight")
    List<String> highlights;
  @Singular("contributingAgent")
    List<String> contributingAgents;
}
