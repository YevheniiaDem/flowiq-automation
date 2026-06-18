package com.flowiq.agents.review.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FeatureChange {
    String featureId;
    String featureName;
    FeatureChangeType changeType;
    String module;
    String httpMethod;
    String endpointPath;
    String schemaName;
  @Singular("changedFile")
    List<String> changedFiles;
    String description;
}
