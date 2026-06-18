package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseType;

import java.util.regex.Pattern;

public class BackendInstabilityRootCauseAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public BackendInstabilityRootCauseAnalyzer() {
        super(RootCauseType.BACKEND_INSTABILITY,
                Pattern.compile("(?i)(500|internal server error|service unavailable|HTTP 5\\d\\d|status code: 5)",
                        Pattern.DOTALL),
                "Backend returned server errors — environment instability, deployment issues, or data-dependent failures.",
                0.80);
    }
}
