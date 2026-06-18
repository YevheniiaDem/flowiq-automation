package com.flowiq.agents.maintenance.model;

public enum MaintenanceSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    public int penaltyPoints() {
        return switch (this) {
            case CRITICAL -> 12;
            case HIGH -> 8;
            case MEDIUM -> 4;
            case LOW -> 2;
        };
    }
}
