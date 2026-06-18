package com.flowiq.agents.llm;

import com.flowiq.agents.config.AgentConfig;
import com.flowiq.agents.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Claude provider stub — ready for HTTP client integration.
 * Set agent.llm.provider=claude and agent.llm.claude.api.key to enable.
 */
@Slf4j
public class ClaudeLlmProvider implements LlmProvider {

    private final AgentConfig agentConfig;

    public ClaudeLlmProvider(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Override
    public String name() {
        return "claude";
    }

    @Override
    public boolean isConfigured() {
        String apiKey = agentConfig.claudeApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public Optional<String> enrichAnalysis(AnalysisResult result) {
        if (!isConfigured()) {
            log.warn("Claude provider selected but agent.llm.claude.api.key is not configured");
            return Optional.empty();
        }
        log.info("Claude enrichment requested with model {} (integration pending)", agentConfig.claudeModel());
        return Optional.of("Claude analysis pending integration. "
                + "Configure HTTP client to call model " + agentConfig.claudeModel() + ".");
    }
}
