package com.flowiq.agents.factory.scorer;

import com.flowiq.agents.factory.model.FactoryCategory;
import com.flowiq.agents.factory.model.FactoryDimensionSummary;

import java.util.List;
import java.util.Map;

public class QualityFactoryScoreCalculator {

    private static final Map<String, Double> WEIGHTS = Map.of(
            "PR Health", 0.25,
            "Governance Health", 0.25,
            "Failure Intelligence", 0.20,
            "Release Readiness", 0.20,
            "Test Intelligence", 0.10);

    public int calculate(List<FactoryDimensionSummary> dimensions) {
        if (dimensions.isEmpty()) {
            return 0;
        }
        double weighted = 0.0;
        double totalWeight = 0.0;
        for (FactoryDimensionSummary dimension : dimensions) {
            Double weight = WEIGHTS.get(dimension.getName());
            if (weight == null) {
                continue;
            }
            weighted += dimension.getHealthScore() * weight;
            totalWeight += weight;
        }
        if (totalWeight == 0.0) {
            return 0;
        }
        return (int) Math.round(weighted / totalWeight);
    }

    public FactoryCategory categorize(int score) {
        return FactoryCategory.fromScore(score);
    }
}
