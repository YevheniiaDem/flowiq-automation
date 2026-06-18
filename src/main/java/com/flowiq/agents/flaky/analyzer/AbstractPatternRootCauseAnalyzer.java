package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseHypothesis;
import com.flowiq.agents.flaky.model.RootCauseType;
import com.flowiq.agents.flaky.model.TestExecutionRecord;

import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractPatternRootCauseAnalyzer implements RootCauseAnalyzer {

    private final RootCauseType type;
    private final Pattern pattern;
    private final String description;
    private final double confidence;

    protected AbstractPatternRootCauseAnalyzer(RootCauseType type, Pattern pattern,
                                               String description, double confidence) {
        this.type = type;
        this.pattern = pattern;
        this.description = description;
        this.confidence = confidence;
    }

    @Override
    public RootCauseType type() {
        return type;
    }

    @Override
    public RootCauseHypothesis analyze(String combinedFailureText, List<TestExecutionRecord> failureRuns) {
        if (combinedFailureText != null && pattern.matcher(combinedFailureText).find()) {
            return RootCauseHypothesis.builder()
                    .type(type)
                    .description(description)
                    .confidence(confidence)
                    .build();
        }
        return null;
    }
}
