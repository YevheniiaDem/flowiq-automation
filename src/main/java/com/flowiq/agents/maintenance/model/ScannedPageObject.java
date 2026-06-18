package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScannedPageObject {
    String className;
    String filePath;
    String source;
    int lineCount;
    int publicMethodCount;
}
