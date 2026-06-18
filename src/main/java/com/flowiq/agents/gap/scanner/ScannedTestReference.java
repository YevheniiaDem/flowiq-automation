package com.flowiq.agents.gap.scanner;

import com.flowiq.agents.model.TestSuiteType;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class ScannedTestReference {
    String className;
    String module;
    Set<TestSuiteType> suites;
    String method;
    String path;
    boolean negativeScenario;
    boolean authorizationCheck;
    String sourceHint;
}
