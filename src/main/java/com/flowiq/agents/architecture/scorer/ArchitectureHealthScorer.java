package com.flowiq.agents.architecture.scorer;

import com.flowiq.agents.architecture.model.ArchitectureDriftIssue;
import com.flowiq.agents.architecture.model.DriftSeverity;

import java.util.List;

public class ArchitectureHealthScorer {

    public int score(List<ArchitectureDriftIssue> issues) {
        int dtoPenalty = issues.stream()
                .filter(issue -> issue.getType() == com.flowiq.agents.architecture.model.DriftIssueType.DTO_WITHOUT_SCHEMA)
                .mapToInt(this::penaltyFor)
                .sum();
        int otherPenalty = issues.stream()
                .filter(issue -> issue.getType() != com.flowiq.agents.architecture.model.DriftIssueType.DTO_WITHOUT_SCHEMA)
                .mapToInt(this::penaltyFor)
                .sum();
        int totalPenalty = Math.min(40, dtoPenalty) + otherPenalty;
        return Math.max(0, 100 - Math.min(100, totalPenalty));
    }

    private int penaltyFor(ArchitectureDriftIssue issue) {
        return switch (issue.getSeverity()) {
            case CRITICAL -> 15;
            case HIGH -> 8;
            case MEDIUM -> 4;
            case LOW -> 2;
        };
    }

    public int countBySeverity(List<ArchitectureDriftIssue> issues, DriftSeverity severity) {
        return (int) issues.stream().filter(i -> i.getSeverity() == severity).count();
    }
}
