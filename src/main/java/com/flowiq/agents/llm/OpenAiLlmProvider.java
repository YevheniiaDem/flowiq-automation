package com.flowiq.agents.llm;

import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * OpenAI provider stub — ready for HTTP client integration.
 * Set agent.llm.provider=openai and agent.llm.openai.api.key to enable.
 */
@Slf4j
public class OpenAiLlmProvider implements LlmProvider {

    private final AgentConfig agentConfig;

    public OpenAiLlmProvider(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public boolean isConfigured() {
        String apiKey = agentConfig.openAiApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public Optional<String> enrichAnalysis(AnalysisResult result) {
        if (!isConfigured()) {
            log.warn("OpenAI provider selected but agent.llm.openai.api.key is not configured");
            return Optional.empty();
        }
        log.info("OpenAI enrichment requested with model {} (integration pending)", agentConfig.openAiModel());
        return Optional.of("OpenAI analysis pending integration. "
                + "Configure HTTP client to call model " + agentConfig.openAiModel() + ".");
    }
}
