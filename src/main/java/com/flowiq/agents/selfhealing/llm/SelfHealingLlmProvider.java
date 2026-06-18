package com.flowiq.agents.selfhealing.llm;

import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;

import java.util.Optional;

/**
 * Optional LLM enrichment for locator suggestions.
 * Default: {@link NoOpSelfHealingLlmProvider}.
 */
public interface SelfHealingLlmProvider {

    String name();

    boolean isConfigured();

    Optional<LocatorSuggestion> enrichSuggestion(LocatorFailureContext context, LocatorSuggestion baseline);
}
