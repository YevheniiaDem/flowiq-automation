package com.flowiq.agents.llm;

import com.flowiq.agents.model.AnalysisResult;

import java.util.Optional;

public class NoOpLlmProvider implements LlmProvider {

    @Override
    public String name() {
        return "none";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public Optional<String> enrichAnalysis(AnalysisResult result) {
        return Optional.empty();
    }
}
