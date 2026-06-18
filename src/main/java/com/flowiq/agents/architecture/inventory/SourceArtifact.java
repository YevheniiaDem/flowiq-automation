package com.flowiq.agents.architecture.inventory;

import lombok.Value;

@Value
public class SourceArtifact {
    String name;
    String module;
    String relativePath;
}
