package com.flowiq.agents.orchestrator.model;

public enum QualityCategory {
    EXCELLENT,
    GOOD,
    NEEDS_ATTENTION,
    CRITICAL;

    public static QualityCategory fromScore(int score) {
        if (score >= 85) {
            return EXCELLENT;
        }
        if (score >= 70) {
            return GOOD;
        }
        if (score >= 50) {
            return NEEDS_ATTENTION;
        }
        return CRITICAL;
    }
}
