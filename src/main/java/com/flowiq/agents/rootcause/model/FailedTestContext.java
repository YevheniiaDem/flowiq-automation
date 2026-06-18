package com.flowiq.agents.rootcause.model;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.nio.file.Path;
import java.util.List;

@Value
@Builder
public class FailedTestContext {
    TestExecutionRecord execution;
  @Singular("screenshot")
    List<Path> screenshots;
  @Singular("trace")
    List<Path> traces;
  @Singular("video")
    List<Path> videos;
  @Singular("backendLogLine")
    List<String> backendLogLines;
}
