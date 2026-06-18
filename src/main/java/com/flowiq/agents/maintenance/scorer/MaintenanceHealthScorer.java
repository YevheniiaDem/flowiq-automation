package com.flowiq.agents.maintenance.scorer;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceHealthCategory;

import java.util.List;

public class MaintenanceHealthScorer {

    public int score(List<MaintenanceFinding> findings) {
        int penalty = findings.stream().mapToInt(f -> f.getSeverity().penaltyPoints()).sum();
        return Math.max(0, 100 - Math.min(100, penalty));
    }

    public MaintenanceHealthCategory categorize(int score) {
        return MaintenanceHealthCategory.fromScore(score);
    }
}
