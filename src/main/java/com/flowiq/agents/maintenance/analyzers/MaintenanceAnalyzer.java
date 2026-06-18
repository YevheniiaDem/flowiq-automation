package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.List;

public interface MaintenanceAnalyzer {

    String name();

    List<MaintenanceFinding> analyze(MaintenanceContext context);
}
