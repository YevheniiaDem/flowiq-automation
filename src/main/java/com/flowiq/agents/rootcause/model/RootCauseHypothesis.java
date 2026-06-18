package com.flowiq.agents.rootcause.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RootCauseHypothesis {
    RootCauseCategory category;
    String description;
    int confidence;
  @Singular("evidenceItem")
    List<String> evidence;
}
