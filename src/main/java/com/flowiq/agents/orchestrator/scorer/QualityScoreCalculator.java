package com.flowiq.agents.orchestrator.scorer;

import com.flowiq.agents.orchestrator.model.QualityCategory;
import com.flowiq.agents.orchestrator.model.QualityDimensionSummary;

import java.util.List;
import java.util.Map;

public class QualityScoreCalculator {

    private static final Map<String, Double> WEIGHTS = Map.of(
            "API Health", 0.15,
            "Coverage Health", 0.20,
            "Release Risk", 0.20,
            "Architecture Health", 0.15,
            "Flaky Status", 0.15,
            "Regression Risk", 0.10,
            "Traceability Status", 0.05);

    public int calculate(List<QualityDimensionSummary> dimensions) {
        if (dimensions.isEmpty()) {
            return 0;
        }
        double weighted = 0.0;
        double totalWeight = 0.0;
        for (QualityDimensionSummary dimension : dimensions) {
            double weight = WEIGHTS.getOrDefault(dimension.getName(), 1.0 / dimensions.size());
            weighted += dimension.getHealthScore() * weight;
            totalWeight += weight;
        }
        if (totalWeight <= 0.0) {
            return 0;
        }
        return (int) Math.round(weighted / totalWeight);
    }

    public QualityCategory categorize(int qualityScore) {
        return QualityCategory.fromScore(qualityScore);
    }
}
