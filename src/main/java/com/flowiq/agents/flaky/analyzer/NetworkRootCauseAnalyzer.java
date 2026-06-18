package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseType;

import java.util.regex.Pattern;

public class NetworkRootCauseAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public NetworkRootCauseAnalyzer() {
        super(RootCauseType.NETWORK_INSTABILITY,
                Pattern.compile("(?i)(connection refused|connection reset|socket|unknown host|network|SSL|TLS|502|503|504)",
                        Pattern.DOTALL),
                "Network-level instability between test runner and application under test.",
                0.82);
    }
}
