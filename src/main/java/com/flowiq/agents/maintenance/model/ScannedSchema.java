package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScannedSchema {
    String fileName;
    String filePath;
}
