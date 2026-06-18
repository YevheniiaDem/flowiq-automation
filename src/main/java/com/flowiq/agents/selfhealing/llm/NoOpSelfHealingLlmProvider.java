package com.flowiq.agents.selfhealing.llm;

import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;

import java.util.Optional;

public class NoOpSelfHealingLlmProvider implements SelfHealingLlmProvider {

    @Override
    public String name() {
        return "none";
    }

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public Optional<LocatorSuggestion> enrichSuggestion(LocatorFailureContext context, LocatorSuggestion baseline) {
        return Optional.of(baseline);
    }
}
