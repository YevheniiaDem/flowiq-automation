package com.flowiq.agents.selfhealing.model;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.List;

@Value
@Builder
public class LocatorFailureContext {
    String testKey;
    String testName;
    String className;
    String methodName;
    String failureMessage;
    String stackTrace;
    String oldLocator;
    LocatorType oldLocatorType;
    Path screenshotPath;
    Path domSnapshotPath;
    List<DomElement> domElements;
}
