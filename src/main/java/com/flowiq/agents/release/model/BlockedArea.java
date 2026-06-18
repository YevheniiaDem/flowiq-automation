package com.flowiq.agents.release.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class BlockedArea {
    String module;
    String reason;
    int failureCount;
  @Singular
    List<String> affectedSuites;
  @Singular
    List<String> affectedTests;
}
