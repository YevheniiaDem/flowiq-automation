package com.flowiq.agents.llm;

import com.flowiq.agents.model.AnalysisResult;

import java.util.Optional;

/**
 * Abstraction for LLM-powered impact analysis enrichment.
 * Implementations: {@link NoOpLlmProvider}, {@link OpenAiLlmProvider}, {@link ClaudeLlmProvider}.
 */
public interface LlmProvider {

    String name();

    boolean isConfigured();

    Optional<String> enrichAnalysis(AnalysisResult result);
}
