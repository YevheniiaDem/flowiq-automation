package com.flowiq.agents.orchestrator.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QualityAgentRunResult {
    QualityAgentType agentType;
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

    public static QualityAgentRunResult success(QualityAgentType type, Object payload, long durationMs) {
        return QualityAgentRunResult.builder()
                .agentType(type)
                .success(true)
                .message("Completed successfully")
                .durationMs(durationMs)
                .payload(payload)
                .build();
    }

    public static QualityAgentRunResult failure(QualityAgentType type, String message, long durationMs) {
        return QualityAgentRunResult.builder()
                .agentType(type)
                .success(false)
                .message(message)
                .durationMs(durationMs)
                .build();
    }
}
