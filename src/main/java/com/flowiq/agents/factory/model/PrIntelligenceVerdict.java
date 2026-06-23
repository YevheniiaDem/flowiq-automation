package com.flowiq.agents.factory.model;

public enum PrIntelligenceVerdict {
    APPROVED,
    APPROVED_WITH_RISK,
    REJECTED;

    public static PrIntelligenceVerdict worst(PrIntelligenceVerdict a, PrIntelligenceVerdict b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
