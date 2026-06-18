package com.flowiq.agents.prreview.model;

public enum PrReviewSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    public static PrReviewSeverity max(PrReviewSeverity left, PrReviewSeverity right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
