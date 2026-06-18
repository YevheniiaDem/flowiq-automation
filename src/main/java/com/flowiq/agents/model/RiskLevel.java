package com.flowiq.agents.model;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    public static RiskLevel fromChanges(java.util.Collection<ApiChange> changes) {
        if (changes.stream().anyMatch(ApiChange::isBreaking)) {
            long breakingCount = changes.stream().filter(ApiChange::isBreaking).count();
            return breakingCount >= 3 ? HIGH : MEDIUM;
        }
        if (changes.isEmpty()) {
            return LOW;
        }
        return changes.size() >= 5 ? MEDIUM : LOW;
    }
}
