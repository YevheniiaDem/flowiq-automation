package com.flowiq.agents.prreview.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PrChangedArtifact {
    String artifactId;
    String name;
    PrChangedArtifactType type;
    String module;
    String httpMethod;
    String endpointPath;
    String schemaName;
    String filePath;
    String sourceContent;
    @Singular("relatedFile")
    List<String> relatedFiles;
    String description;
    boolean newlyAdded;
}
