package com.flowiq.agents.architecture.inventory;

import lombok.Value;

import java.nio.file.Path;
import java.util.List;

@Value
class SourceCodeInventory {
    List<SourceArtifact> services;
    List<SourceArtifact> controllers;
    List<SourceArtifact> pages;
    List<SourceArtifact> dtos;
    List<Path> schemaFiles;
}
