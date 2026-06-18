package com.flowiq.agents.selfhealing.llm;

import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;

public final class SelfHealingLlmProviderFactory {

    private SelfHealingLlmProviderFactory() {
    }

    public static SelfHealingLlmProvider create(SelfHealingAgentConfig config) {
        String provider = config.llmProvider();
        if (provider == null || provider.isBlank() || "none".equalsIgnoreCase(provider)) {
            return new NoOpSelfHealingLlmProvider();
        }
        return switch (provider.toLowerCase()) {
            case "openai" -> new OpenAiSelfHealingLlmProvider(config);
            case "claude" -> new OpenAiSelfHealingLlmProvider(config);
            default -> throw new IllegalArgumentException("Unsupported self-healing LLM provider: " + provider);
        };
    }
}
