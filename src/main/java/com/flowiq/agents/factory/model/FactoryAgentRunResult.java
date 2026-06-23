package com.flowiq.agents.factory.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FactoryAgentRunResult {
    FactoryAgentType agentType;
    boolean success;
    String message;
    long durationMs;
    Object payload;

    public <T> T payloadAs(Class<T> type) {
        if (payload == null || !type.isInstance(payload)) {
            return null;
        }
        return type.cast(payload);
    }

    public static FactoryAgentRunResult success(FactoryAgentType type, Object payload, long durationMs) {
        return FactoryAgentRunResult.builder()
                .agentType(type)
                .success(true)
                .message("Completed successfully")
                .durationMs(durationMs)
                .payload(payload)
                .build();
    }

    public static FactoryAgentRunResult failure(FactoryAgentType type, String message, long durationMs) {
        return FactoryAgentRunResult.builder()
                .agentType(type)
                .success(false)
                .message(message)
                .durationMs(durationMs)
                .build();
    }
}
