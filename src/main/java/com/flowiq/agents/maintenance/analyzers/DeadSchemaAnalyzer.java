package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.model.MaintenanceFindingType;
import com.flowiq.agents.maintenance.model.MaintenanceSeverity;
import com.flowiq.agents.maintenance.model.ScannedSchema;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeadSchemaAnalyzer implements MaintenanceAnalyzer {

    @Override
    public String name() {
        return "DeadSchemaAnalyzer";
    }

    @Override
    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        String sources = context.getCombinedMainAndTestSources().toLowerCase(Locale.ROOT);
        List<MaintenanceFinding> findings = new ArrayList<>();

        for (ScannedSchema schema : context.getSchemas()) {
            String fileName = schema.getFileName().toLowerCase(Locale.ROOT);
            String stem = fileName.replace(".schema.json", "")
                    .replace("-schema.json", "")
                    .replace(".json", "");
            if (!sources.contains(fileName) && !sources.contains(stem)) {
                findings.add(MaintenanceFinding.builder()
                        .type(MaintenanceFindingType.DEAD_CODE)
                        .severity(MaintenanceSeverity.LOW)
                        .title("Schema not referenced")
                        .location(schema.getFilePath())
                        .recommendation("Remove orphan schema " + schema.getFileName()
                                + " or add contract tests validating it.")
                        .priorityRank(3)
                        .build());
            }
        }
        return findings;
    }
}
