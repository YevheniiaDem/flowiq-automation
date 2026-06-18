package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class NetworkFailureAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public NetworkFailureAnalyzer() {
        super(RootCauseCategory.NETWORK,
                Pattern.compile("(?i)(connection refused|connection reset|unknown host|socket timeout"
                        + "|SSLHandshakeException|TLS|502\\s+bad gateway|503\\s+service unavailable"
                        + "|504\\s+gateway timeout|ECONNREFUSED|read timed out|ConnectException)",
                        Pattern.DOTALL),
                "Network connectivity or upstream service reachability issue between test runner and SUT.",
                82);
    }
}
