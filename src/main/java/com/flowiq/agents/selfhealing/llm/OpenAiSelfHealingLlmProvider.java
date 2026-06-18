package com.flowiq.agents.selfhealing.llm;

import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import com.flowiq.agents.selfhealing.model.LocatorSuggestion;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Placeholder for future OpenAI-powered locator refinement.
 */
@Slf4j
public class OpenAiSelfHealingLlmProvider implements SelfHealingLlmProvider {

    private final SelfHealingAgentConfig config;

    public OpenAiSelfHealingLlmProvider(SelfHealingAgentConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public boolean isConfigured() {
        String key = config.openAiApiKey();
        return key != null && !key.isBlank();
    }

    @Override
    public Optional<LocatorSuggestion> enrichSuggestion(LocatorFailureContext context, LocatorSuggestion baseline) {
        log.info("OpenAI self-healing LLM integration is not yet wired; returning baseline suggestion");
        return Optional.of(baseline);
    }
}
