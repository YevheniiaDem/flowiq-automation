package com.flowiq.agents.llm;

import com.flowiq.agents.config.AgentConfig;
import org.aeonbits.owner.ConfigFactory;

public final class LlmProviderFactory {

    private LlmProviderFactory() {
    }

    public static LlmProvider create() {
        return create(ConfigFactory.create(AgentConfig.class));
    }

    public static LlmProvider create(AgentConfig agentConfig) {
        String provider = agentConfig.llmProvider();
        if (provider == null || provider.isBlank() || "none".equalsIgnoreCase(provider)) {
            return new NoOpLlmProvider();
        }
        return switch (provider.toLowerCase()) {
            case "openai" -> new OpenAiLlmProvider(agentConfig);
            case "claude" -> new ClaudeLlmProvider(agentConfig);
            default -> throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
        };
    }
}
