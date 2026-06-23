package com.flowiq.agents.factory.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class FactoryAgentResultsBundle {

    private final Map<FactoryAgentType, FactoryAgentRunResult> results = new EnumMap<>(FactoryAgentType.class);

    public void add(FactoryAgentRunResult result) {
        results.put(result.getAgentType(), result);
    }

    public Optional<FactoryAgentRunResult> get(FactoryAgentType type) {
        return Optional.ofNullable(results.get(type));
    }

    public <T> Optional<T> payload(FactoryAgentType type, Class<T> clazz) {
        return get(type)
                .filter(FactoryAgentRunResult::isSuccess)
                .map(r -> r.payloadAs(clazz));
    }

    public Map<FactoryAgentType, FactoryAgentRunResult> all() {
        return Map.copyOf(results);
    }
}
