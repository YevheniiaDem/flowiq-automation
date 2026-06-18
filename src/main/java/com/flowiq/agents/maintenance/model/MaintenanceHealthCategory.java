package com.flowiq.agents.maintenance.model;

public enum MaintenanceHealthCategory {
    EXCELLENT,
    GOOD,
    WARNING,
    CRITICAL;

    public static MaintenanceHealthCategory fromScore(int score) {
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
