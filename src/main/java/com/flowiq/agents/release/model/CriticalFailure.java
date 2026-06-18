package com.flowiq.agents.release.model;

import com.flowiq.agents.model.TestSuiteType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CriticalFailure {
    String testKey;
    String className;
    String methodName;
    TestSuiteType suiteType;
    String module;
    String message;
    String source;
    FailureSeverity severity;
}
