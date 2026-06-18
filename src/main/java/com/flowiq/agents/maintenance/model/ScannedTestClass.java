package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class ScannedTestClass {
    String className;
    String filePath;
    String source;
    int lineCount;
    int methodCount;
    int maxMethodLines;
    @Singular("endpointKey")
    Set<String> endpointKeys;
    @Singular("assertion")
    List<String> assertions;
}
