package com.flowiq.agents.traceability.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class BusinessFeature {
    String module;
    String displayName;
  @Singular("docSource")
    Set<String> docSources;
    String description;
}
