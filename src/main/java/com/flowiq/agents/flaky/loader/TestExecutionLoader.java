package com.flowiq.agents.flaky.loader;

import com.flowiq.agents.flaky.model.TestExecutionRecord;

import java.util.List;

public interface TestExecutionLoader {

    String sourceName();

    List<TestExecutionRecord> load();
}
