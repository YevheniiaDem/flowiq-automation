package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedDto;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;

public class DeadDtoAnalyzer implements MaintenanceAnalyzer {

    @Override
    public String name() {
        return "DeadDtoAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        String sources = context.getCombinedMainAndTestSources();
        List<MaintenanceFinding> findings = new ArrayList<>();

        for (ScannedDto dto : context.getDtos()) {
            if (!isReferenced(dto.getClassName(), sources)) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DEAD_CODE)
                        .severity(MaintenanceSeverity.MEDIUM)
                        .title("DTO not referenced")
                        .location(dto.getFilePath())
                        .recommendation("Remove unused DTO " + dto.getClassName()
                                + " or wire it into services/tests.")
                        .priorityRank(2)
                        .build());
            }
        }
        return findings;
    }

    private static boolean isReferenced(String className, String sources) {
        return sources.contains(className);
    }
}
