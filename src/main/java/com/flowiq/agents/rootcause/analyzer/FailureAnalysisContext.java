package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.FailedTestContext;

import java.util.List;

public record FailureAnalysisContext(
        FailedTestContext failedTest,
        String combinedFailureText,
        List<String> backendLogExcerpt) {

    public static FailureAnalysisContext from(FailedTestContext context) {
        var record = context.getExecution();
        String combined = safe(record.getMessage()) + "\n" + safe(record.getStackTrace());
        if (!context.getBackendLogLines().isEmpty()) {
            combined = combined + "\n" + String.join("\n", context.getBackendLogLines());
        }
        return new FailureAnalysisContext(context, combined, context.getBackendLogLines());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
