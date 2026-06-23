package com.flowiq.agents.factory.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FactoryDimensionSummary {
    String name;
    int healthScore;
    @Singular("highlight")
    List<String> highlights;
    @Singular("contributingComponent")
    List<String> contributingComponents;
}
