package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class DataIssueAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public DataIssueAnalyzer() {
        super(RootCauseCategory.DATA,
                Pattern.compile("(?i)(expected:.*but was:|AssertionFailedError|assertEquals failed"
                        + "|fixture not found|test data|seed data|no rows found|empty result"
                        + "|JSON path.*does not match|schema validation failed)",
                        Pattern.DOTALL),
                "Test data mismatch, stale fixture, or assertion on incorrect expected values.",
                78);
    }

    @Override
    protected int adjustConfidence(int confidence, FailureAnalysisContext context) {
        String text = context.combinedFailureText();
        if (text.contains("expected:") && text.contains("but was:")) {
            return confidence + 10;
        }
        return confidence;
    }
}
