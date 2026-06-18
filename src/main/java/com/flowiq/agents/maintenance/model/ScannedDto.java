package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScannedDto {
    String className;
    String filePath;
}
