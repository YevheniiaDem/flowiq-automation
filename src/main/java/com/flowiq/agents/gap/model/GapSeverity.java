package com.flowiq.agents.gap.model;

public enum GapSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    public static GapSeverity max(GapSeverity left, GapSeverity right) {
        return left.ordinal() <= right.ordinal() ? left : right;
    }
}
