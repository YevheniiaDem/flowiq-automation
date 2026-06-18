package com.flowiq.agents.orchestrator.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class QualityAgentResultsBundle {

    private final Map<QualityAgentType, QualityAgentRunResult> results = new EnumMap<>(QualityAgentType.class);

    public void add(QualityAgentRunResult result) {
        results.put(result.getAgentType(), result);
    }

    public Optional<QualityAgentRunResult> get(QualityAgentType type) {
        return Optional.ofNullable(results.get(type));
    }

    public <T> Optional<T> payload(QualityAgentType type, Class<T> clazz) {
        return get(type)
                .filter(QualityAgentRunResult::isSuccess)
                .map(r -> r.payloadAs(clazz));
    }

    public Map<QualityAgentType, QualityAgentRunResult> all() {
        return Map.copyOf(results);
    }
}
