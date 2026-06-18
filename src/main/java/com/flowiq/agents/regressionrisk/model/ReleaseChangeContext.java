package com.flowiq.agents.regressionrisk.model;

import com.flowiq.agents.model.ApiChange;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class ReleaseChangeContext {
  @Singular("changedFile")
    List<String> changedFiles;
  @Singular("apiChange")
    List<ApiChange> apiChanges;
  @Singular("backendModule")
    Set<String> backendModules;
  @Singular("frontendModule")
    Set<String> frontendModules;
}
