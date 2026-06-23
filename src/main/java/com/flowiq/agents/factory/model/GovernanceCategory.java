package com.flowiq.agents.factory.model;

public enum GovernanceCategory {
    EXCELLENT,
    GOOD,
    WARNING,
    CRITICAL;

    public static GovernanceCategory fromScore(int score) {
        if (score >= 85) {
            return EXCELLENT;
        }
        if (score >= 70) {
            return GOOD;
        }
        if (score >= 50) {
            return WARNING;
        }
        return CRITICAL;
    }
}
