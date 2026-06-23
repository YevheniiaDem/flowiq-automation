package com.flowiq.agents.factory.model;

public enum FactoryCategory {
    EXCELLENT,
    GOOD,
    NEEDS_ATTENTION,
    CRITICAL;

    public static FactoryCategory fromScore(int score) {
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
