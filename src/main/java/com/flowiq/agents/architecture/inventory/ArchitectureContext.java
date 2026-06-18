package com.flowiq.agents.architecture.inventory;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class ArchitectureContext {
  @Singular("openApiEndpoint")
    List<ApiEndpointRef> openApiEndpoints;
  @Singular("documentedEndpoint")
    List<ApiEndpointRef> documentedEndpoints;
  @Singular
    List<SourceArtifact> services;
  @Singular
    List<SourceArtifact> controllers;
  @Singular
    List<SourceArtifact> pages;
  @Singular
    List<SourceArtifact> dtos;
  @Singular
    List<Path> schemaFiles;
    Set<String> contractTestClasses;
    Set<String> regressionTestClasses;
    Set<String> smokeTestClasses;
    Set<String> uiTestClasses;
    Set<String> documentedModules;
}
